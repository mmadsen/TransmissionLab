#!/usr/bin/perl -w
#
# Copyright (c) 2007, Mark E. Madsen, Alex Bentley, and Carl P. Lipo. All Rights Reserved.
#
# This code is offered for use under the terms of the Creative Commons-GNU General Public License
# http://creativecommons.org/licenses/GPL/2.0/
#
# Our intent in licensing this software under the CC-GPL is to provide freedom for researchers, students,
# and other interested parties to replicate our research results, pursue their own research, etc.  You are, however,
# free to use the code contained in this package for whatever purposes you wish, provided you adhere to the
# open license terms specified in LICENSE and GPL.txt
#
# See the files LICENSE and GPL.txt in the top-level directory of this source archive for the license
# details and grant.
#

use WWW::Mechanize;
use Storable;


my $url = 'http://www.ssa.gov/OACT/babynames/';
open(MALEFILE,">malebirth.txt") or die "Can't open malebirth.txt";
print MALEFILE "Year/tRank/tMale Name/tNumber of Births/n";
open(FEMALEFILE,">femalebirth.txt") or die "Can't open femalebirth.txt";
print FEMALEFILE "Year/tRank/tMale Name/tNumber of Births/n";
my $year;

for ($year = 1880; $year < 2007; $year ++) {
print "Now on year $year \n";
my $m = WWW::Mechanize->new();
    $m->get($url);
      my $fields = {
        'year' => $year,
        'top' => 1000,
	'number' => "n"
      };
	$m->form_number(2);
 	my $r = $m->submit_form(form_number => 2,
                             fields => $fields);
      die "Couldn't submit form" unless $r->is_success;
    my $c = $m->content;

    $c =~ m{<table width=\"72%.(.*?)</table>}s
      or die "Can't find the birth name table\n";
    my $t = $1;
    my @outer = $t =~ m{<tr.*?>(.*?)</tr>}gs;
    shift @outer;
	my @data;
    foreach $r (@outer) {
      my @bits = $r =~ m{<td.*?>(.*?)</td>}gs;
      for (my $x = 0; $x < @bits; $x++) {
        my $b = $bits[$x];
        my @v = split /\s*<BR>\s*/, $b;
        foreach (@v) { s/^\s+//; s/\s+$// }
        push @{$data[$x]}, @v;
      }
    }

	# males
    for (my $y = 0; $y < @{$data[0]}; $y++) {
	print MALEFILE $year, "\t", $data[0][$y], "\t", $data[1][$y], "\t", comma_free($data[2][$y]),"\n";
	}	
	# females
    for (my $y = 0; $y < @{$data[0]}; $y++) {
	print FEMALEFILE $year, "\t", $data[0][$y], "\t", $data[3][$y], "\t", comma_free($data[4][$y]),"\n";
	}	

}
close MALEFILE;
close FEMALEFILE;

    sub comma_free {
      my $n = shift;
      $n =~ s/,//;
      return $n;
    };

