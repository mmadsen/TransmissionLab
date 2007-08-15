#!/usr/bin/perl 

# TransmissonLab Version 1.X
#
# process-multiple-run-stats.pl
#
# (c) 2007 Mark E. Madsen
# Distributed under the terms of the Creative Commons-GNU General Public License
# See files LICENSE and GPL.txt in the TransmissionLab distribution for license details.
#
# Purpose:  script to post-process the results of the OverallStatisticsRecorder data 
# collector class.  That file will often contain multiple replicates at the same values of 
# numAgents and Mu, but different random number seeds.  This script averages the MeanTurnover,
# MeanTraitCount, MeanAgentsInTopN, and MeanTraitResidenceTime values for each set of replicates that share the same 
# ListSize, NumAgents and MutationRate.  
#
# This script works against specific versions of output from OverallStatisticsRecorder and is included in the 
# source distribution of each specific "tagged" version.  
#
# The results are written to STDOUT and can be redirected into another 
# file, and contain every column except the random seed, which are no longer meaningful after 
# averaging across rows.
#
# Usage:  perl process-multiple-run-stats.pl <input file>  >  <output file> 
# 
# Statistics::Descriptive from CPAN is a dependency; you will see an error message if your Perl
# installation does not have this module.  The module is available at:
# http://search.cpan.org/dist/Statistics-Descriptive/  and easily installable using the CPAN shell
# which comes with your Perl installation.   
#

#use strict;
use constant FALSE => 0;
use constant TRUE => 1;

use Data::Dumper;
use Statistics::Descriptive;

my @rows = ();
my $header_seen = FALSE;


# read rows from the input file, split into fields on tabs, ignore the header row
while(<>) {
	if ( $header_seen == FALSE ) {
		if ( $_ =~ /^NumAgents\tMutationRate/ ) {
			print STDERR "(debug) Header row seen, file is transmissionlab output\n";
			$header_seen = TRUE;
			next;
		}
	}
	
	my $parsed_fields = parse_row( $_ );
	#print STDERR "(debug) meanturnover: ", $parsed_fields->{ "meanturnover"}, "\n";
	push @rows, $parsed_fields;
}

if ($header_seen == FALSE ) {
	print STDERR "ERROR: input file processed and does not contain header; may not be transmissionlab output\n";
	exit 1;
}



#print Dumper( @rows );
#print STDERR "(debug) number of rows to process: ", scalar @rows, "\n";



# now take the parsed input rows, and process to average any parameter replicates
my $processed_data = process_replicates( \@rows );

#print Dumper( $processed_data );

# write a header row to STDOUT representing the new data output
write_header();


# now take the reduced data set and produce a new tab-delimited output file
create_final_output( $processed_data );


exit;

############## Subroutines ###################

sub parse_row {
	my $row = shift;
	my $parsed_row = {};
	my @fields = split /\t/, $row;
	$parsed_row->{"numagents"} = shift @fields;
	$parsed_row->{"mutationrate"} = shift @fields;
	$parsed_row->{"numticks"} = shift @fields;
	$parsed_row->{"rngseed"} = shift @fields;
	$parsed_row->{"topnlistsize"} = shift @fields;
	$parsed_row->{"meanturnover"} = shift @fields;
	$parsed_row->{"stdevturnover"} = shift @fields;
	$parsed_row->{"meantraitcount"} = shift @fields;
	$parsed_row->{"stdevtraitcount"} = shift @fields;
	$parsed_row->{"meanagentstopn"} = shift @fields;
	$parsed_row->{"stdevagentstopn"} = shift @fields;
	$parsed_row->{"meanresidencetime"} = shift @fields;
	$parsed_row->{"stdevresidencetime"} = shift @fields;
	return $parsed_row;
}

# process_replicates takes a reference to an array containing rows which are each
# a hash table with key=value pairs for the columns in the input file.  These 
# individual rows are then examined for replicates.  This is most easily done by 
# creating a "tree" of the data -- the first level hash contains numAgents values as keys,
# the second level mutationRate values as keys.  For simplicity in this version, we assume
# that these two parameters uniquely identify a replicate -- for example, one should 
# not mix two different "types" of runs in the same data file -- e.g., runs with 
# other parameters not held constant.  If this is a serious problem we may need more
# infrastructure in the output dumps in the future to capture parameter state and 
# thus allow us to sort out these things in post-processing.  
#
# each "leaf" node in the combination topnlistsize -> numAgents -> mu then holds a hash with keys
# "MeanTurnover" and "MeanTraitCount", the values of which are Statistics::Descriptive
# objects established the first time we see a particular numAgents/MutationRate combo.  
# if, when processing a given row we already have a pair of Statistics::Descriptive objects at
# a particular leaf node, we simply add the current row's meanTurnover and meantraitCount 
# values to each statistics object and move on.
# 
# The net result at the end of the routine is that we have a large batch of S::D objects
# full of replicates, ready to be queried for the mean and standard deviation of their replicates.

sub process_replicates {
	my $row_list = shift;
	my $processed_data = {};
	foreach my $row ( @$row_list ) {
		
		# let's simplify the below and allow easier debugging...
		my $listsize = $row->{"topnlistsize"};
		my $numagents = $row->{"numagents"};
		my $mu = $row->{"mutationrate"};
		my $meanturnover = $row->{"meanturnover"};
		my $meantraitcount = $row->{"meantraitcount"};
		my $meanagentstopn = $row->{"meanagentstopn"};
		my $meanresidencetime = $row->{"meanresidencetime"};
		
		#print STDERR "(debug) processing row with listsize: $listsize  numagents: $numagents  mu: $mu\n";
		
		# print STDERR "(debug) processing row\n";
		
		# first examine numAgents, initialize slot if new
		if ( ! defined $processed_data->{$listsize}) {
			$processed_data->{$listsize} = {};
		}

		if ( ! defined $processed_data->{$listsize}->{$numagents}) {
		     $processed_data->{$listsize}->{$numagents} = {};
		}
		
		# now examine mutationrate in the context of numagents, initialize slot if new
		if ( ! defined $processed_data->{$listsize}->{$numagents}->{$mu} ) {
			$processed_data->{$listsize}->{$numagents}->{$mu} = {};
			$processed_data->{$listsize}->{$numagents}->{$mu}->{"turnover"} = Statistics::Descriptive::Sparse->new();
			$processed_data->{$listsize}->{$numagents}->{$mu}->{"traitcount"} = Statistics::Descriptive::Sparse->new();
			$processed_data->{$listsize}->{$numagents}->{$mu}->{"agentstopn"} = Statistics::Descriptive::Sparse->new();
			$processed_data->{$listsize}->{$numagents}->{$mu}->{"residencetime"} = Statistics::Descriptive::Sparse->new();
		}
		
		# now store the data we care about
		$processed_data->{$listsize}->{$numagents}->{$mu}->{"turnover"}->add_data($meanturnover);
		$processed_data->{$listsize}->{$numagents}->{$mu}->{"traitcount"}->add_data($meantraitcount);
		$processed_data->{$listsize}->{$numagents}->{$mu}->{"agentstopn"}->add_data($meanagentstopn);
		$processed_data->{$listsize}->{$numagents}->{$mu}->{"residencetime"}->add_data($meanresidencetime);
	}
	
	return $processed_data;
}

# header row for the output
sub write_header {
	print STDOUT "TopNListSize\tNumAgents\tMutationRate\tReplicates\tMeanTurnover\tStdevTurnover\tMeanTraitCount\tStdevTraitCount\tMeanAgentsTopN\tStdevAgentsTopN\tMeanResidenceTime\tStdevResidenceTime\n";
}

# create_final_output runs through the processed replicates, in dually sorted order:
# numAgents low -> high, and for each value, mutationRate low -> high.  For each unique
# combination, we write a tab-delimited output line to STDOUT with the numAgents, mutationRate,
# and replicate-average meanTurnover, stdevTurnover, and replicate-average MeanTraitCount,
# stdevTraitCount.  

sub create_final_output {
	my $processed_data = shift;
	my @topnlistsize_list = sort { $a <=> $b } keys %$processed_data;
	
	foreach my $topnlistsize_value ( @topnlistsize_list ) {
		my @numagent_list = sort { $a <=> $b } keys %{$processed_data->{ $topnlistsize_value }};
		foreach my $numagent_value ( @numagent_list ) {

		    my @mutationrate_list = sort { $a <=> $b } keys %{ $processed_data->{ $topnlistsize_value }->{ $numagent_value }};

		    foreach my $mutationrate_value ( @mutationrate_list ) {
		        print $topnlistsize_value, "\t";
                print $numagent_value, "\t";
                print $mutationrate_value, "\t";
                print $processed_data->{$topnlistsize_value}->{$numagent_value}->{$mutationrate_value}->{"turnover"}->count(), "\t";
                print $processed_data->{$topnlistsize_value}->{$numagent_value}->{$mutationrate_value}->{"turnover"}->mean(), "\t";
                print $processed_data->{$topnlistsize_value}->{$numagent_value}->{$mutationrate_value}->{"turnover"}->standard_deviation(), "\t";
                print $processed_data->{$topnlistsize_value}->{$numagent_value}->{$mutationrate_value}->{"traitcount"}->mean(), "\t";
                print $processed_data->{$topnlistsize_value}->{$numagent_value}->{$mutationrate_value}->{"traitcount"}->standard_deviation(), "\t";
                print $processed_data->{$topnlistsize_value}->{$numagent_value}->{$mutationrate_value}->{"agentstopn"}->mean(), "\t";
                print $processed_data->{$topnlistsize_value}->{$numagent_value}->{$mutationrate_value}->{"agentstopn"}->standard_deviation(), "\t";
                print $processed_data->{$topnlistsize_value}->{$numagent_value}->{$mutationrate_value}->{"residencetime"}->mean(), "\t";
                print $processed_data->{$topnlistsize_value}->{$numagent_value}->{$mutationrate_value}->{"residencetime"}->standard_deviation(), "\n";
			}
		}
	}
}



