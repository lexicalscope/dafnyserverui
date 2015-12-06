grammar DafnyServer;

verificationArtifact
	: logLine
	| modularVerificationResult
	| verificationCompleted
	;
	
verificationCompleted
	: 'Verification completed successfully!' NEWLINE '[SUCCESS] [[DAFNY-SERVER: EOM]]' NEWLINE
	;
	
modularVerificationResult 
	: 'Verifying' NEWLINE LINE NEWLINE; 

logLine
	: FILE ;
	
FILE : ~'('*? '(' INT ',' INT '):' .*?;

LINE : ~['\r'?'\n']* NEWLINE;

INT : DIGIT+;

fragment NEWLINE:'\r'? '\n' ;

fragment DIGIT : [0-9] ;
