#! /usr/bin/perl
## Perl tool to analyze the distribution of profile match scores

# Version 1.0 25 May 2011 Peter E. Midford
# Version 1.1 4 March 2012 Peter E. Midford - mods for using HyperSS
# Version 1.2 22 May 2012 Peter E. Midford - mods for meanIC (remove HyperSS)

use warnings;
use strict;
use Fcntl;

my $line;
my $count = 0;

open(F,"ProfileMatchReport.txt") or die "can't open the profile match report ";
open(D0_50, ">","ProfileScores0_50.txt") or die "can't open file for Range 0 - 50";
print D0_50 "Taxon	Gene	Taxon phenotypes	Gene phenotypes	MaxIC	95%	99%	decile	meanIC	95%	99%	decile	\n";
open(D50_90, ">","ProfileScores50_90.txt") or die "can't open file for Range 50 - 90";
print D50_90 "Taxon	Gene	Taxon phenotypes	Gene phenotypes	MaxIC	95%	99%	decile	meaIC	95%	99%	decile	\n";
open(D90_100, ">","ProfileScores90_100.txt") or die "can't open file for Range 90 - 100";
print D90_100 "Taxon	Gene	Taxon phenotypes	Gene phenotypes	MaxIC	95%	99%	decile	meanIC	95%	99%	decile	\n";

my $header = <F>;         # read and ignore?
my $columnheaders = <F>;  # read and ignore?
my @hist = (0,0,0,0,0,0,0,0,0,0,0,0);
while($line = <F>){
  $count += 1;
  my (@line) = split('\t',$line);
  print "line with less than 13 fields \n" if (@line < 13);
  print "line with more than 13 fields \n" if (@line > 13);
  if(@line == 13){
      my $decile = $line[11];
      if ($decile == 0){
	  $hist[0] += 1;
	  print D0_50 $line
      }
      if ($decile==1){
	  $hist[1] += 1;
	  print D0_50 $line
      }
      if ($decile==2){
	  $hist[2] += 1;
	  print D0_50 $line
      } 
      if ($decile==3){
	  $hist[3] += 1;
	  print D0_50 $line
      }
      if ($decile==4){
	  $hist[4] += 1; 
	  print D0_50 $line
      }
      if ($decile==5){
	  $hist[5] += 1;
	  print D50_90 $line
      }
      if ($decile==6){
	  $hist[6] += 1;
	  print D50_90 $line
      } 
      if ($decile==7){
	  $hist[7] += 1;
	  print D50_90 $line
      }
      if ($decile==8){
	  $hist[8] += 1;
	  print D50_90 $line
      }
      if ($decile==9){
	  $hist[9] += 1;
	  print D90_100 $line
      }
      if ($decile==10){
	  $hist[10] += 1;
	  print D90_100 $line
      } 
      if ($decile==11){
	  $hist[11] += 1; 
	  print D90_100 $line
      }
  }
  print $count,"   ",$line[6],"\n" if $count % 10000 == 0;
}
close(F);
close(D0_50);
close(D50_90);
close(D90_100);



my $upper = 10;
my $lower = 0;
foreach(@hist){
  print $lower,"/100 <= meanIC < ",$upper,"/100   $_\n";
  if ($upper < 90){
  	$upper += 10;
  	$lower += 10;
  }
  elsif ($upper == 99){
	$upper = 100;
	$lower = 99;
  }
  elsif ($upper == 95) {
	$upper = 99;
	$lower = 95;
  }
  elsif ($upper == 90) {
	$upper = 95;
	$lower = 90;
  }
}


