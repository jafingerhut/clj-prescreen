: # use perl -*-Perl-*-
eval 'exec perl -S "$0" ${1+"$@"}'
    if 0;
# -*cperl-*-

use strict;
use Getopt::Long;
use File::Basename;
# File::Temp is a library for creating temporary files.
use File::Temp;
# FindBin is a library to find the directory in which the script is
# installed.  This can be useful for making it easy to install other
# auxiliary files needed by the script all in one place, and no matter
# where someone installs 'the whole package' of files, the script can
# find them, without needing hand-editing.
use FindBin;
# Below is an example use of the $FindBin::Bin string variable defined
# by the line above, to include the 'build' subdirectory beneath that
# in the list of directories searched when finding .pm files for later
# 'use' statements.  Useful if a .pm file is part of 'the whole
# package' you want to distribute with this script.
use lib "$FindBin::Bin" . "/build";


#my $debug = 1;

my $verbose = 0;
my $full_progname = $0;
my $progname = fileparse($full_progname);

sub usage {
    print STDERR
"usage: $progname [ --help ] [ --verbose ]
               [ file1 ... ]

description here
";
}

my $help = 0;
if (!GetOptions('help' => \$help,
		'verbose+' => \$verbose
		))
{
    usage();
    exit 1;
}
if ($help) {
    usage();
    exit 0;
}

my $users = [ ];

while (my $line = <>) {
    chomp $line;
    if ($line =~ /^\s*<tr class="vcard/) {
	my $user = { };

	$line = <>;
	chomp $line;
	if ($line =~ /<span class="username">(\S+)<\/span>/) {
	    $user->{'username'} = $1;
	} else {
	    die sprintf "Expecting line %d to contain class=\"username\" but found the following instead:\n%s\n", $., $line;
	}
	
	$line = <>;
	chomp $line;
	if ($line =~ /^\s*<td>\s*$/) {
	    # Skip it
	} else {
	    die sprintf "Expecting line %d to contain <td> but found the following instead:\n%s\n", $., $line;
	}
	
	$line = <>;
	chomp $line;
	if ($line =~ /^\s*<span class="fn">(.*)<\/span>/) {
	    $user->{'full_name'} = $1;
	} else {
	    die sprintf "Expecting line %d to contain <span class=\"fn\">full name</span> but found the following instead:\n%s\n", $., $line;
	}
	
	$line = <>;
	chomp $line;
	if ($line =~ /<span class="email">(.*)<\/span>/) {
	    $user->{'email'} = $1;
	} else {
	    die sprintf "Expecting line %d to contain <span class=\"email\">email address</span> but found the following instead:\n%s\n", $., $line;
	}

	while ($line = <>) {
	    chomp $line;
	    if ($line =~ /^\s*<\/tr>\s*$/) {
		last;
	    }
	    # else keep reading until we find that line
	}
	
	push @{$users}, $user;
    } else {
	# Skip it
    }
}

printf "[\n";
foreach my $user (@{$users}) {
    printf "{:display-name \"%s\"\n", $user->{'full_name'};
    printf " :usernames #{ \"%s\" }\n", $user->{'username'};
    printf " :emails #{ \"%s\" }\n", $user->{'email'};
    printf " }\n";
}
printf "]\n";

exit 0;
