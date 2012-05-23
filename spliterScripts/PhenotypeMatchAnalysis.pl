#! /usr/bin/perl
## Perl tool to analyze the distribution of phenotype match scores

# Version 1.0 4 Mar 2011 Peter E. Midford

use warnings;
use strict;
use Fcntl;

my $line;
my $count = 0;

open(F,"PhenotypeMatchReport.txt") or die "can't open the match report ";
open(IC0, ">","MatchScores0.txt") or die "can't open file for IC=0";
print IC0 "Taxon	Gene	Taxon entity	Gene entity	Shared quality	Common EQ subsumer with lowest IC (not necessarily unique)	IC	simJ	simcIC\n";
open(IC01, ">","MatchScores01.txt") or die "can't open file for 0<IC<=1";
print IC01 "Taxon	Gene	Taxon entity	Gene entity	Shared quality	Common EQ subsumer with lowest IC (not necessarily unique)	IC	simJ	simcIC\n";
open(IC12, ">","MatchScores12.txt") or die "can't open file for 1<IC<=2";
print IC12 "Taxon	Gene	Taxon entity	Gene entity	Shared quality	Common EQ subsumer with lowest IC (not necessarily unique)	IC	simJ	simcIC\n";
open(IC23, ">","MatchScores23.txt") or die "can't open file for 2<IC<=3";
print IC23 "Taxon	Gene	Taxon entity	Gene entity	Shared quality	Common EQ subsumer with lowest IC (not necessarily unique)	IC	simJ	simcIC\n";
open(IC34, ">","MatchScores34.txt") or die "can't open file for 3<IC<=4";
print IC34 "Taxon	Gene	Taxon entity	Gene entity	Shared quality	Common EQ subsumer with lowest IC (not necessarily unique)	IC	simJ	simcIC\n";
open(IC45, ">","MatchScores45.txt") or die "can't open file for 4<IC<=5";
print IC45 "Taxon	Gene	Taxon entity	Gene entity	Shared quality	Common EQ subsumer with lowest IC (not necessarily unique)	IC	simJ	simcIC\n";
open(IC56, ">","MatchScores56.txt") or die "can't open file for 5<IC<=6";
print IC56 "Taxon	Gene	Taxon entity	Gene entity	Shared quality	Common EQ subsumer with lowest IC (not necessarily unique)	IC	simJ	simcIC\n";
open(IC67, ">","MatchScores67.txt") or die "can't open file for 6<IC<=7";
print IC67 "Taxon	Gene	Taxon entity	Gene entity	Shared quality	Common EQ subsumer with lowest IC (not necessarily unique)	IC	simJ	simcIC\n";
open(IC78, ">","MatchScores78.txt") or die "can't open file for 7<IC<=8";
print IC78 "Taxon	Gene	Taxon entity	Gene entity	Shared quality	Common EQ subsumer with lowest IC (not necessarily unique)	IC	simJ	simcIC\n";
open(IC89, ">","MatchScores89.txt") or die "can't open file for 8<IC<=9";
print IC89 "Taxon	Gene	Taxon entity	Gene entity	Shared quality	Common EQ subsumer with lowest IC (not necessarily unique)	IC	simJ	simcIC\n";
open(IC910, ">","MatchScores910.txt") or die "can't open file for 9<IC<=10";
print IC910 "Taxon	Gene	Taxon entity	Gene entity	Shared quality	Common EQ subsumer with lowest IC (not necessarily unique)	IC	simJ	simcIC\n";
open(IC1011, ">","MatchScores1011.txt") or die "can't open file for 10<IC<=11";
print IC1011 "Taxon	Gene	Taxon entity	Gene entity	Shared quality	Common EQ subsumer with lowest IC (not necessarily unique)	IC	simJ	simcIC\n";
open(IC1112, ">","MatchScores1112.txt") or die "can't open file for 11<IC<=12";
print IC1112 "Taxon	Gene	Taxon entity	Gene entity	Shared quality	Common EQ subsumer with lowest IC (not necessarily unique)	IC	simJ	simcIC\n";
open(IC1213, ">","MatchScores1213.txt") or die "can't open file for 12<IC<=13";
print IC1213 "Taxon	Gene	Taxon entity	Gene entity	Shared quality	Common EQ subsumer with lowest IC (not necessarily unique)	IC	simJ	simcIC\n";
open(IC1314, ">","MatchScores1314.txt") or die "can't open file for 13<IC<=14";
print IC1314 "Taxon	Gene	Taxon entity	Gene entity	Shared quality	Common EQ subsumer with lowest IC (not necessarily unique)	IC	simJ	simcIC\n";
open(IC1415, ">","MatchScores1415.txt") or die "can't open file for 14<IC<=15";
print IC1415 "Taxon	Gene	Taxon entity	Gene entity	Shared quality	Common EQ subsumer with lowest IC (not necessarily unique)	IC	simJ	simcIC\n";

my $header = <F>;         # read and ignore?
my $columnheaders = <F>;  # read and ignore?
my @hist = (0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
while($line = <F>){
  $count += 1;
  my (@line) = split('\t',$line);
  print "line with less than 7 fields \n" if (@line < 10);
  print "line with more than 7 fields \n" if (@line > 10);
  my $score = $line[6];
  if ($score == 0){
    $hist[0] += 1;
    print IC0 $line
  }
  if ($score>0 && $score<=1){
    $hist[1] += 1;
    print IC01 $line
  }
  if ($score>1 && $score<=2){
    $hist[2] += 1;
    print IC12 $line
  } 
  if ($score>2 && $score<=3){
    $hist[3] += 1;
    print IC23 $line
  }
  if ($score>3 && $score<=4){
    $hist[4] += 1; 
    print IC34 $line
  }
  if ($score>4 && $score<=5){
    $hist[5] += 1;
    print IC45 $line
  }
  if ($score>5 && $score<=6){
    $hist[6] += 1;
    print IC56 $line
  } 
  if ($score>6 && $score<=7){
    $hist[7] += 1;
    print IC67 $line
  }
  if ($score>7 && $score<=8){
    $hist[8] += 1;
    print IC78 $line
  }
  if ($score>8 && $score<=9){
    $hist[9] += 1;
    print IC89 $line
  }
  if ($score>9 && $score<=10){
    $hist[10] += 1;
    print IC910 $line
  } 
  if ($score>10 && $score<=11){
    $hist[11] += 1; 
    print IC1011 $line
  }
  if ($score>11 && $score<=12){
    $hist[12] += 1;
    print IC1112 $line
  }
  if ($score>12 && $score<=13){
    $hist[13] += 1;
    print IC1213 $line
  }
  if ($score>13 && $score<=14){
    $hist[14] += 1;
    print IC1314 $line
  }
  if ($score>14 && $score<=15){
    $hist[15] += 1;
    print IC1415 $line
  } 
  print $count,"   ",$line[6],"\n" if $count % 10000 == 0;
}
close(F);
close(IC0);
close(IC01);
close(IC12);
close(IC23);
close(IC34);
close(IC45);
close(IC56);
close(IC67);
close(IC78);
close(IC89);
close(IC910);
close(IC1011);
close(IC1112);
close(IC1213);
close(IC1314);
close(IC1415);



my $upper = 0;
my $lower = -1;
foreach(@hist){
  print $lower," < IC <= ",$upper,"   $_\n";
  $upper += 1;
  $lower += 1;
}


