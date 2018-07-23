grammar Grammar;

r:program;

program
	:(function
	| var 
	| expr
	| print
	| loop
	| control
	| funcReturn)*
	;

function
	: ('void'|type|'List<'type'>') ID '(' (param (',' param)*)? ')''{' program '}' 	#DeclareFunc
	| ID '(' (expr (',' expr)*)? ')'															#FuncCall
	;
	
funcReturn
	: 'RETURN' expr 				#ReturnData
	;
	
param
	: type ID						#Parameter
	| 'const' type ID				#constParameter
	| 'List<' type '>' ID			#ListParameter
	;

expr
	: 'ADD' '(' expr ',' expr ')' 	#AddFunc
	| 'SUB' '(' expr ',' expr ')' 	#SubFunc
	| 'MUL' '(' expr ',' expr ')' 	#MulFunc
	| 'DIV' '(' expr ',' expr ')' 	#DivFunc
	| primitives					#PrimitiveTypes
	| ID							#Variable
	| listControl					#ControlList
	| function						#Func				
	;

control
	: 'IF' '(' expr operator expr ')' '{' program '}' ('ELSE' '{' program '}')? 	#IFStatement
	;
	
loop
	: 'FOR' '(' var ',' ID operator expr ',' expr ')' '{' program '}' 	#ForLoop
	| 'WHILE' '(' expr operator expr ')' '{' program '}'				#WhileLoop
	;
	
print
	: 'PRINT' '(' expr (',' expr)* ')' 			#PrintFunc
	;
	
var
	: type ID '=' expr				#Initialise
	| 'const' type ID '=' expr		#ConstInitialise
	| ID '=' ID						#AssignFromVariable
	| ID '=' expr					#AssignFromExpression
	| list							#ListVar
	;
	
list
	: 'List<' type '>' ID '=' '[' expr (',' expr)* ']' 	#InitialiseListWithData
	| 'List<' type'>' ID 								#InitialiseList
	;
	
listControl
	: 'LIST_ADD(' ID ',' expr ')'						#AddToList
	| 'LIST_REMOVEAT(' ID ',' expr ')'					#RemoveFromList
	| 'LIST_SETAT(' ID ',' expr ',' expr ')'			#SetInList
	| 'LIST_CLEAR(' ID ')'								#ClearList
	| 'LIST_SIZE(' ID ')'								#GetListSize
	| 'LIST_GET(' ID ',' expr ')'						#GetFromList
	;
	
type
	: 'int'
	| 'double'
	| 'bool'
	| 'string'
	;
	
primitives
	: INT							#Int
	| DOUBLE						#Double
	| BOOL							#Bool
	| STRING						#String
	;
	
operator
	: '>'
	| '<'		
	| '=='
	| '!='
	| '>='
	| '<='
	;
INT 	: ('-')?[0-9]+ ;
DOUBLE 	: ('-')?[0-9]+ ('.' [0-9]+)? ;
BOOL	: [0-1] | 'false' | 'true';
STRING 	: '"' .*? '"';
ID 		: [a-zA-Z][a-zA-Z0-9]* ;
WS 		: [ \n\t\r\f]+ -> skip ;
	 