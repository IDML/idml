grammar Mapping;

@header { package io.idml.lang; }

document : section* |
           mapping*;

section : header mapping*;

header : '[' label ']';

mapping : assignment  |
          variable |
          rootAssignment |
          reassignment;

assignment   : destination '=' pipeline;
variable     : 'let' destination '=' pipeline;
rootAssignment : Root '=' pipeline;
reassignment : destination ':' callChain;

destination : label ('.' label)*;

array : '[]' # emptyArray | '[' pipeline (',' pipeline)* ','? ']' # arrayWithStuffIn;

object : '{}' # emptyObject | '{' String ':' pipeline  (',' String ':' pipeline)* ','? '}' #objectWithStuffIn;

pipeline : match # matchExp |
           ifExpression # ifExp |
           literalExpression # literalExp |
           variableExpression # variableExp |
           tempVariableExpression # tempVariableExp |
           thisRelativePathExpression # thisRelativePathExp |
           relativePathExpression # relativePathExp |
           absolutePathExpression # absolutePathExp |
           arrayPathExpression # arrayPathExp |
           objectPathExpression # objectPathExp |
           pipeline ForwardSlash pipeline # Division |
           pipeline Wildcard pipeline # Multiplication |
           pipeline Plus pipeline # Addition |
           pipeline Minus pipeline # Subtraction ;

caseBlock : '|' predicate '=>' pipeline;
match : 'match ' pipeline caseBlock*;
ifExpression : 'if' predicate 'then' pipeline ('else' pipeline)?;

literalExpression          : filter? literal modifier* ('.' expressionChain)?;
variableExpression         : filter? '@' part modifier* ('.' expressionChain)?;
tempVariableExpression     : filter? '$' part modifier* ('.' expressionChain)?;
relativePathExpression     : expressionChain;
thisRelativePathExpression : filter? This modifier* ('.' expressionChain)?;
absolutePathExpression     : filter? Root modifier* ('.' expressionChain)?;
arrayPathExpression        : filter? array modifier* ('.' expressionChain)?;
objectPathExpression       : filter? object modifier* ('.' expressionChain)?;

expressionChain        : filter? part modifier* ('.' expressionChain)*;
callChain              : function ('.' function)*;

index  : '[' Minus?Int ']';
slice  : '[' sliceLeft ':' sliceRight ']';
filter : '[' predicate ']';
sliceLeft  : Int?;
sliceRight : Int?;

modifier : index | slice | filter;

part : function | label | coalesce | Any | Wildcard;

function : label '(' arguments?  ')';

coalesce : '(' pipeline ('|' pipeline)* ')';

arguments : argument (',' argument)* ;

argument : pipeline | predicate;

literal : Minus?Int | String | Minus?Float | Boolean;


predicate : Underscore |
            unitary |
            binary |
            negation |
            predicate Or predicate |
            predicate And predicate |
            grouped;

unitary     : pipeline ExistsOp;
binary      : pipeline CaseSensitive? (SubStrOp | EqualsOp | NotEqualsOp | InOp | ContainsOp | LessThanOp | GreaterThanOp | LessThanOrEqualOp | GreaterThanOrEqualOp) pipeline;
negation    : 'not' predicate;
grouped     : '(' predicate ')';

/* This is higher than label so that Boolean literals are evaluated before labels */
Boolean         : 'true' | 'false';

label : PlainLabel | BacktickLabel;

Underscore        : '_';
LessThanOp        : '<';
GreaterThanOp     : '>';
LessThanOrEqualOp : '<=';
GreaterThanOrEqualOp : '>=';
ForwardSlash    : '/';
Minus           : '-';
Plus            : '+';
This            : 'this';
Root            : 'root';
And             : 'and';
Or              : 'or';
Any             : '**';
Wildcard        : '*';
Comment         : '#' ~( '\r' | '\n' )* -> skip;
CaseSensitive   : 'cs';
SubStrOp        : 'substr';
EqualsOp        : '==';
NotEqualsOp     : '!=';
InOp            : 'in';
ContainsOp      : 'contains';
ExistsOp        : 'exists';
PlainLabel      : [a-zA-Z_][a-zA-Z_0-9]*;
BacktickLabel   : '`' ~[<`]* '`';
Whitespace      : [ \t\n\r]+ -> skip;
Int             : [0-9]+;
String          : '"'  ( EscapeSeq | ~( '\\'|'\n'|'"' ) )* '"'
                | '\'' ( EscapeSeq | ~( '\\'|'\n'|'\'' ) )* '\''
                | '"""' ( EscapeSeq | ~( '\\') )* '"""';

Float           : [0-9]+ '.' [0-9]*;

fragment EscapeSeq : '\\' . ;


