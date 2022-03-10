grammar MappingTest;

document     : (testOptions | testMappings | testCase)* text?;

// Grammar rules for interpreter actions
testOptions  : text? Options;
testMappings : text? Mappings;
testCase     : text? Input text? (Output | Exception | Metrics);

// Grammar rules for documentation
text         : (header | other)+;
header       : Header;
other        : Any+;

// Comments
Comment1   : '//' ~( '\r' | '\n' )* -> skip;
Comment2   : '/*' .*? '*/'          -> skip;

// Lexer symbols for interpreter actions
Mappings  : '+++' .*? '+++';
Input     : '<<<' .*? '<<<';
Output    : '>>>' .*? '>>>';
Exception : '!!!' .*? '!!!';
Metrics   : '~~~' .*? '~~~';
Options   : '@@@' .*? '@@@';

// Lexer symbols for documentation
Header    : '#' ~( '\r' | '\n' )*;
Any       : .+?;
