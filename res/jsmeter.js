var JSMETER = {};

//tokens.js
//2010-01-14

//(c) 2006 Douglas Crockford

//Produce an array of simple token objects from a string.
//A simple token object contains these members:
//   type: 'name', 'string', 'number', 'operator'
//   value: string or number value of the token
//   from: index of first character of the token
//   to: index of the last character + 1

//Comments of the // type are ignored.

//Operators are by default single characters. Multicharacter
//operators can be made by supplying a string of prefix and
//suffix characters.
//characters. For example,
//   '<>+-&', '=>&:'
//will match any of these:
//   <=  >>  >>>  <>  >=  +: -: &: &&: &&

JSMETER.setup = function() {

 // Make a new object that inherits members from an existing object.

 if (typeof Object.create !== 'function') {
     Object.create = function (o) {
         function F() {}
         F.prototype = o;
         return new F();
     };
 }
 
 // Transform a token object into an exception object and throw it.
 
 Object.prototype.error = function (message, t) {
     t = t || this;
     t.name = "SyntaxError";
     t.message = message;
     //debugger;
     //throw t;
 };

 String.prototype.tokens = function (prefix, suffix) {
     var operators = [
         "+",
         "-",
         "*",
         "/",
         "%",
         "<",
         ">",
         "=",
         "==",
         "===",
         "!",
         "!=",
         "!==",
         "<=",
         ">=",
         ".",
         "&",
         "&&",
         "|",
         "||",
         "^",
         ">>",
         ">>>",
         "<<",
         "++",
         "--",
         "+=",
         "-=",
         "*=",
         "/=",
         "%=",
         "<<=",
         ">>=",
         ">>>=",
         "&=",
         "|=",
         "^=",
         "~",
         "?",
         ":"
     ];
     var operatorCache = { };
     var c;                      // The current character.
     var cc;					 // character code, charCodeAt() - by realrap
     var from;                   // The index of the start of the token.
     var i = 0;                  // The index of the current character.
     var l = 1;                  // The current line number
     var origI = -1;             // i before tried to be regexp
     var length = this.length;
     var n;                      // The number value.
     var q;                      // The quote character.
     var str;                    // The string value.
     var isHex;                  // Keeps track of whether a number is hex
 
     var result = [];            // An array to hold the results.
     
     var that = this;
 
     var lastNonWhite = function() {
         var j = i - 1;
         while (" \t\n\r".indexOf(that.charAt(j))>=0 && j>=0) {
             j--;
         }
         return that.charAt(j);
     };
     
     var lastToken = function() {
         return result[result.length-1];
     };
     
     var isOperator = function(op) {
         if (typeof operatorCache[op]!=='undefined') {
             return operatorCache[op];
         }
         for (var oi=0; oi<operators.length; oi++) {
             if (operators[oi]===op) {
                 operatorCache[op]=true;
                 //console.log("is " + op);
                 return true;
             }
         }
         operatorCache[op]=false;
         //console.log("is not " + op);
         return false;
     };
 
     var make = function (type, value) {
 
 // Make a token object.
 
         return {
             type: type,
             value: value,
             from: from,
             to: i,
             line: l
         };
     };
 
 // Begin tokenization. If the source string is empty, return nothing.
 
     if (!this) {
         return;
     }
 
 // If prefix and suffix strings are not provided, supply defaults.
 
     if (typeof prefix !== 'string') {
         prefix = '<>+-&';
     }
     if (typeof suffix !== 'string') {
         suffix = '=>&:';
     }
 
 
 // Loop through this text, one character at a time.
 
     c = this.charAt(i);
     while (c && i < this.length) {
         //console.log(i);
         from = i;
         cc = this.charCodeAt(i);
         
 // Ignore whitespace.
 
         if (c <= ' ') {
             i += 1;
             if (c==='\n' || cc === 13) {	// 개행 확인을 위해 cc == 13 추가 
                 l++;
             }
             c = this.charAt(i);
 // name.
 
         } else if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c==="_" || c==="$") {
             str = c;
             i += 1;
             for (;;) {
                 c = this.charAt(i);
                 if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
                         (c >= '0' && c <= '9') || c === '_' || c==="$") {
                     str += c;
                     i += 1;
                 } else {
                     break;
                 }
             }
             result.push(make('name', str));
 
 // number.
 
 // A number can start with a decimal point. It may start with a digit,
 // possibly '0'.
 
         } else if ((c >= '0' && c <= '9') || (c==="." && this.charAt(i+1)>='0' && this.charAt(i+1)<='9')) {
             isHex = false;
             str = c;
             origI = i;
             i += 1;
 
 // Look for more digits.
 
             for (;;) {
                 c = this.charAt(i);
                 if (c==='x' && i-origI===1) {
                     //console.log("isHex");
                     isHex = true;
                 } else if (c < '0' || c > '9') {
                     if (!isHex || c.toLowerCase()<'a' || c.toLowerCase()>'f') {
                         break;
                     }
                 }
                 i += 1;
                 str += c;
             }
 
 // Look for a decimal fraction part.
 
             if (c === '.') {
                 i += 1;
                 str += c;
                 for (;;) {
                     c = this.charAt(i);
                     if (c < '0' || c > '9') {
                         break;
                     }
                     i += 1;
                     str += c;
                 }
             }
 
 // Look for an exponent part.
 
             if (c === 'e' || c === 'E') {
                 i += 1;
                 str += c;
                 c = this.charAt(i);
                 if (c === '-' || c === '+') {
                     i += 1;
                     str += c;
                 }
                 if (c < '0' || c > '9') {
                     make('number', str).error("Bad exponent");
                 }
                 do {
                     i += 1;
                     str += c;
                     c = this.charAt(i);
                 } while (c >= '0' && c <= '9');
             }
 
 // Make sure the next character is not a letter.
 
             if (c >= 'a' && c <= 'z') {
                 str += c;
                 i += 1;
                 make('number', str).error("Bad number");
             }
 
 // Convert the string value to a number. If it is finite, then it is a good
 // token.
 
             n = +str;
             if (isFinite(n)) {
                 result.push(make('number', n));
             } else {
                 make('number', str).error("Bad number");
             }
 
 // string
 
         } else if (c === '\'' || c === '"') {
             str = '';
             q = c;
             i += 1;
             for (;;) {
                 c = this.charAt(i);
                 if (c < ' ') {
                     make('string', str).error(c === '\n' || c === '\r' || c === '' ?
                         "Unterminated string." :
                         "Control character in string.", make('', str));
                 }
 
 // Look for the closing quote.
 
                 if (c === q) {
                     break;
                 }
 
 // Look for escapement.
 
                 if (c === '\\') {
                     i += 1;
                     if (i >= length) {
                         make('string', str).error("Unterminated string");
                     }
                     c = this.charAt(i);
                     switch (c) {
                     case 'b':
                         c = '\b';
                         break;
                     case 'f':
                         c = '\f';
                         break;
                     case 'n':
                         c = '\n';
                         break;
                     case 'r':
                         c = '\r';
                         break;
                     case 't':
                         c = '\t';
                         break;
                     case 'u':
                         if (i >= length) {
                             make('string', str).error("Unterminated string");
                         }
                         c = parseInt(this.substr(i + 1, 4), 16);
                         if (!isFinite(c) || c < 0) {
                             make('string', str).error("Unterminated string");
                         }
                         c = String.fromCharCode(c);
                         i += 4;
                         break;
                     }
                 }
                 str += c;
                 i += 1;
             }
             i += 1;
             result.push(make('string', str));
             c = this.charAt(i);
 
 // regular expression literal
 
         } else if (i>origI && c === '/' && "/*".indexOf(this.charAt(i+1))<0 && ("(=[!:;&|*+-%".indexOf(lastNonWhite())>=0 || lastToken().value==="return")) {
        	 
        	 var ops = '';
             origI = i;
             str = '';
             i += 1;
             for (;;) {
                 c = this.charAt(i);
                 //if ("\n\r".indexOf(c)>=0) {	// Rhino 에서 제대로 인식 못함.
                 if ("\n" == c || "\r" == c) {
                     i = origI;
                     c = '/';
                     break;
                     //make('string', str).error("Invalid regular expression");
                 }
                 
     // look for ending /
                 var unescstr = str.replace(/\\\\/g, "@");
                 //console.log("/" + unescstr + "/");
                 if (c === "/"){ //&& unescstr.charAt(unescstr.length-1)!=="\\" ) {
                     break;
                 }
                 str = str + c;
                 i += 1;
             }
             
     // look for options
             
             if (i>origI) {
                 i+=1;
                 c = this.charAt(i);
                 while ("gim".indexOf(c)>=0) {
                     ops = ops + c;
                     i+=1;
                     c = this.charAt(i);
                 }
                 
                 result.push(make('regexp', '/' + str + '/' + ops));
             }
 // comment.
 
         } else if (c === '/' && this.charAt(i + 1) === '/') {
             str = c;
             i += 1;
             for (;;) {
                 c = this.charAt(i);
                 cc = this.charCodeAt(i);
                 if (c === '\n' || c === '\r' || c === '') {
                     break;
                 }
                 
                 // 자바에서 삽입된 line.separator 는 코드값이 13이다.
                 if(cc === 13){
                	 break;
                 }
                 
                 str += c;
                 i += 1;
             }
             
             result.push(make('comment', str));
 
 //  multi-line comment
 
         } else if (c === '/' && this.charAt(i + 1) === '*') {
             str = c;
             i += 1;
             for (;;) {
                 c = this.charAt(i);
                 cc = this.charCodeAt(i);
                 if (c === '' || (c === '*' && this.charAt(i + 1) === '/')) {
                     i += 2
                     c = this.charAt(i);
                     cc = this.charCodeAt(i);
                     break;
                 }
                 if (c==='\n' || cc === 13) {	// by realrap
                     l++;
                 }
                 str += c;
                 i += 1;
             }
             
             result.push(make('comment', str));
 
 // combining
 
         /*} else if (prefix.indexOf(c) >= 0) {
             str = c;
             i += 1;
             while (i < length) {
                 c = this.charAt(i);
                 if (suffix.indexOf(c) < 0 || 
                     (str==='!' && c!=='=') || 
                     (c==='-' && str!=='-') || 
                     (c==='+' && str!=='+')) {
                         break;
                 }
                 str += c;
                 i += 1;
             }
             result.push(make('operator', str));
 
 // single-character operator
 
         } else {
             i += 1;
             result.push(make('operator', c));
             c = this.charAt(i);
         }*/
         
         } else if (isOperator(c)) {
             str = c;
             i += 1;
             while (i<length) {
                 c = this.charAt(i);
                 if (!isOperator(str+c)) {
                     break;
                 }
                 str += c;
                 i += 1;
             }
             result.push(make('operator', str));
         
 // single-character operator
 
         } else {
             i += 1;
             result.push(make('operator', c));
             c = this.charAt(i);
         }
     }
     return result;
 };

};



//parse.js
//Parser for Simplified JavaScript written in Simplified JavaScript
//From Top Down Operator Precedence
//http://javascript.crockford.com/tdop/index.html
//Douglas Crockford
//2008-07-07

JSMETER.make_parse = function () {
 var scope;
 var symbol_table = {};
 var token;
 var tokens;
 var token_nr;
 var nextComments = [ ];

 var itself = function () {
     return this;
 };

 var original_scope = {
     define: function (n) {
         var t = this.def[n.value];
         /*if (typeof t === "object") {
             n.error(t.reserved ? "Already reserved." : "Already defined.");
         }*/
         this.def[n.value] = n;
         n.reserved = false;
         n.nud      = itself;
         n.led      = null;
         n.std      = null;
         n.lbp      = 0;
         n.scope    = scope;
         return n;
     },
     find: function (n) {
         var e = this, o;
         while (true) {
             o = e.def[n];
             if (o && o.nud) {
                 return o;
             }
             e = e.parent;
             if (!e) {
                 if (!symbol_table.hasOwnProperty(n)) {
                     var s = symbol(n);
                     s.nud = function() {
                         return this;
                     }
                 }
                 return symbol_table[n];
             }
         }
     },
     pop: function () {
         scope = this.parent;
     },
     reserve: function (n) {
         if (n.arity !== "name" || n.reserved) {
             return;
         }
         var t = this.def[n.value];
         if (t) {
             if (t.reserved) {
                 return;
             }
             if (t.arity === "name") {
                 //n.error("Already defined.");
             }
         }
         this.def[n.value] = n;
         n.reserved = true;
     }
 };

 var new_scope = function () {
     var s = scope;
     scope = Object.create(original_scope);
     scope.def = {};
     scope.parent = s;
     return scope;
 };

 /**
  * 기대되는 다음 토큰을 인자로 받고, 그 다음 토큰을 가져온다.
  * 인자가 없다면, 다음 토큰을 가져온다. 
  */  
 var advance = function (id) {
     var a, o, t, v, cl, cli;
     if (id && token.id !== id) {
         token.error("Expected '" + id + "'.");
     }
     if (token_nr >= tokens.length) {
         token = symbol_table["(end)"];
         return;
     }
     t = tokens[token_nr];
     token_nr += 1;
     v = t.value;
     a = t.type;
     if (a === "name") {
         o = scope.find(v);
     } else if (a === "operator") {
         o = symbol_table[v];
         if (!o) {
             t.error("Unknown operator.");
         }
     } else if (a === "string" || a ===  "number" || a === "regexp" || a === "regexpops") {
         o = symbol_table["(literal)"];
         a = "literal";
     } else if (a === "comment") {
         o = symbol_table["(comment)"];
     } else {
         t.error("Unexpected token.");
     }
     token = Object.create(o);
     token.from  = t.from;
     token.to    = t.to;
     token.line  = t.line;
     token.value = v;
     token.arity = a;
     //window.status = JSON.stringify(token);
     
     if (token.arity === "comment") {
         cl = v.split(/\n/g);
         for (cli=0; cli<cl.length; cli++) {
             nextComments.push(cl[cli]);
         }
         advance();
     }
     
     return token;
 };

 var expression = function (rbp) {
     var left;
     var t = token;
     
     advance();
     left = t.nud();
     while (rbp < token.lbp) {
         t = token;
         advance();
         left = t.led(left);
     }  
     if (left) {
         left.comments = nextComments;
         nextComments = [ ];
     }
     return left;
 };

 var statement = function () {
     var n = token, v;

     if (n.std) {
         advance();
         scope.reserve(n);
         return n.std();
     }
     v = expression(0);
     /*if (!v.assignment && 
         v.id !== "(" && 
         v.id!== "++" && 
         v.id!== "--" && 
         v.value!=="use strict" &&
         v.id!=="typeof") {
             v.error("Bad expression statement.");
     }*/
     /*if (v.assignment && v.arity==="function") {
         advance();
     } else {
         advance(";");
     }*/
     
     if (token.id===";") {
         advance(";");
     }  
     if (v) {
         v.comments = nextComments;
         nextComments = [ ];
     }
     return v;
 };

 
 var statements = function () {
     var a = [], s;
     while (true) {
         if (token.id === "}" || token.id === "(end)") {
             break;
         }
         s = statement();
         if (s) {
             a.push(s);
         }
     }
     return a.length === 0 ? null : a.length === 1 ? a[0] : a;
 };

 var block = function () {
     var t = token;
     advance("{");
     return t.std();
 };

 var original_symbol = {
     nud: function () {
         //this.error("Undefined.");
     },
     led: function (left) {
         this.error("Missing operator.");
     }
 };

 var symbol = function (id, bp) {
     var s = symbol_table[id];
     bp = bp || 0;
     if (s) {
         if (bp >= s.lbp) {
             s.lbp = bp;
         }
     } else {
         s = Object.create(original_symbol);
         s.id = s.value = id;
         s.lbp = bp;
         symbol_table[id] = s;
     }
     return s;
 };

 var constant = function (s, v) {
     var x = symbol(s);
     x.nud = function () {
         scope.reserve(this);
         this.value = symbol_table[this.id].value;
         this.arity = "literal";
         return this;
     };
     x.value = v;
     return x;
 };

 var infix = function (id, bp, led) {
     var s = symbol(id, bp);
     s.led = led || function (left) {
         this.first = left;
         this.second = expression(bp);
         this.arity = "binary";
         return this;
     };
     return s;
 };

 var infixr = function (id, bp, led) {
     var s = symbol(id, bp);
     s.led = led || function (left) {
         this.first = left;
         this.second = expression(bp - 1);
         this.arity = "binary";
         return this;
     };
     return s;
 };

 var assignment = function (id) {
     return infixr(id, 10, function (left) {
         if (left.id !== "." && left.id !== "[" && left.arity !== "name") {
             left.error("Bad lvalue.");
         }
         this.first = left;
         this.second = expression(9);
         this.assignment = true;
         this.arity = "binary";
         if (token.id===",") {
             advance(",");
         }
         return this;
     });
 };

 var prefix = function (id, nud) {
     var s = symbol(id);
     s.nud = nud || function () {
         scope.reserve(this);
         this.first = expression(70);
         this.arity = "unary";
         return this;
     };
     return s;
 };

 var stmt = function (s, f) {
     var x = symbol(s);
     x.std = f;
     return x;
 };

 symbol("(end)");
 symbol("(name)");
 symbol(":");
 symbol(";");
 symbol(")");
 symbol("]");
 symbol("}");
 symbol(",");
 symbol("else");

 constant("true", true);
 constant("false", false);
 constant("null", null);
 constant("pi", 3.141592653589793);
 constant("Object", {});
 constant("Array", []);
 constant("Date", "Date");
 constant("Math", "Math");

 symbol("(literal)").nud = itself;
 symbol("(comment)");

 symbol("this").nud = function () {
     scope.reserve(this);
     this.arity = "this";
     return this;
 };

 assignment("=");
 assignment("+=");
 assignment("-=");
 assignment("*=");
 assignment("/=");
 assignment("%=");
 assignment("&=");
 assignment("|=");
 assignment("^=");
 assignment(">>=");
 assignment(">>>=");
 assignment("<<=");

 infix("?", 20, function (left) {
     this.first = left;
     this.second = expression(0);
     advance(":");
     this.third = expression(0);
     this.arity = "ternary";
     return this;
 });

 infixr("&", 20);
 infixr("|", 20);
 
 infixr("&&", 30);
 infixr("||", 30);
 
 infixr("in", 40);
 infixr("==", 40);
 infixr("!=", 40);
 infixr("===", 40);
 infixr("!==", 40);
 infixr("<", 40);
 infixr("<=", 40);
 infixr(">", 40);
 infixr(">=", 40);
 infixr(">>", 40);
 infixr(">>>", 40);
 infixr("<<", 40);

 infixr("instanceof", 45);
 infix("+", 50);
 infix("-", 50);

 infix("^", 60);
 infix("*", 60);
 infix("/", 60);
 infix("%", 60);

 infix("++", 65, function (left) {
         this.first = left;
         this.arity = "unary";
         return this;
     });
 infix("--", 65, function (left) {
         this.first = left;
         this.arity = "unary";
         return this;
     });

 infix(".", 80, function (left) {
     this.first = left;
     //if (token.arity !== "name") {
     //    token.error("Expected a property name.");
     //}
     token.arity = "literal";
     this.second = token;
     this.arity = "binary";
     advance();
     return this;
 });

 infix("[", 80, function (left) {
     this.first = left;
     this.second = expression(0);
     this.arity = "binary";
     advance("]");
     return this;
 });

 infix("(", 80, function (left) {
     var a = [];
     if (left && (left.id === "." || left.id === "[")) {
         this.arity = "ternary";
         this.first = left.first;
         this.second = left.second;
         this.third = a;
     } else {
         this.arity = "binary";
         this.first = left;
         this.second = a;
         /*if ((left.arity !== "unary" || left.id !== "function") &&
                 left.arity !== "name" && left.id !== "(" &&
                 left.id !== "&&" && left.id !== "||" && left.id !== "?" &&
                 left.id !== "function") {
             left.error("Expected a variable name.");
         }*/
     }
     if (token.id !== ")") {
         while (true)  {
             a.push(expression(0));
             if (token.id !== ",") {
                 break;
             }
             advance(",");
         }
     }
     advance(")");
     return this;
 });

 prefix("new");

 prefix("!");
 prefix("~");
 prefix("-");
 prefix("+");
 prefix("--");
 prefix("++");
 prefix("typeof", function() {
     var e = expression(0);
     this.first = e;
     return this;
 });

 prefix("(", function () {
     var e = expression(0);
     advance(")");
     return e;
 });

 prefix("function", function () {
     var a = [];
     new_scope();
     if (token.arity === "name") {
         scope.define(token);
         this.name = token.value;
         advance();
     }
     if (token.id !== "(") {
         scope.define(token);
         this.name = token.value;
         advance();
     }
     advance("(");
     if (token.id !== ")") {
         while (true) {
             if (token.arity !== "name") {
                 token.error("Expected a parameter name.");
             }
             scope.define(token);
             a.push(token);
             advance();
             if (token.id !== ",") {
                 break;
             }
             advance(",");
         }
     }
     this.first = a;
     advance(")");
     this.second = block();
     /*advance("{");
     this.second = statements();
     advance("}");*/
     this.arity = "function";
     this.assignment = true;
     scope.pop();
     return this;
 });

 prefix("[", function () {
     var a = [];
     if (token.id !== "]") {
         while (true) {
             a.push(expression(0));
             if (token.id !== ",") {
                 break;
             }
             advance(",");
         }
     }
     advance("]");
     this.first = a;
     this.arity = "unary";
     return this;
 });

 prefix("{", function () {
     var a = [], n, v;
     if (token.id !== "}") {
         while (true) {
             n = token;
             if (n.arity !== "name" && n.arity !== "literal") {
                 token.error("Bad property name.");
             }
             advance();
             advance(":");
             v = expression(0);
             v.key = n.value;
             a.push(v);
             if (token.id !== ",") {
                 break;
             }
             advance(",");
         }
     }
     advance("}");
     this.first = a;
     this.arity = "unary";
     return this;
 });

 stmt("<script", function() {
     while (token.value!==">") {
         advance();
     }
     advance(">");
 });

 stmt("</script", function() {
     while (token.value!==">") {
         advance();
     }
     advance(">");
 });

 stmt("{", function () {
     new_scope();
     var a = statements();
     advance("}");
     scope.pop();
     return a;
 });

 stmt("var", function () {
     var a = [], n, t;
     while (true) {
         n = token;
         if (n.arity !== "name") {
             n.error("Expected a new variable name.");
         }
         scope.define(n);
         advance();
         if (token.id === "=") {
             t = token;
             advance("=");
             t.first = n;
             t.second = expression(0);
             t.arity = "binary";
             a.push(t);
         }
         if (token.id === "in") {
             t = token;
             advance("in");
             t.first = n;
             t.second = expression(0);
             t.arity = "binary";
             a.push(t);
         }
         if (token.id !== ",") {
             break;
         }
         advance(",");
     }
     if (token.id === ";") {
         advance(";");
     }
     return a.length === 0 ? null : a.length === 1 ? a[0] : a;
 });
 
 stmt("try", function() {
     this.first = block();
     if (token.value === "catch") {
         this.second = statement();
     }
     if (token.value === "finally") {
         //this.third = statement();
     }
     this.arity = "statement";
     return this;
 });
 
 stmt("catch", function() {
     advance("(");
     if (token.id!==")") {
         this.first = expression(0);
     }
     advance(")");
     this.second = block();
     this.arity = "statement";
     return this;
 });
 
 stmt("finally", function() {
     this.first = block();
     this.arity = "statement";
     return this;
 });

 stmt("if", function () {
     advance("(");
     this.first = expression(0);
     advance(")");
     if (token.value==="{") {
         this.second = block();
     } else {
         this.second = statement();
     }
     if (token.id === "else") {
         scope.reserve(token);
         advance("else");
         if (token.id==="if") {
             this.third = statement();
         } else if (token.value==="{") {
             this.third = block();
         } else {
             this.third = statement();
         }
         //this.third = token.id === "if" ? statement() : block();
     } else {
         this.third = null;
     }
     this.arity = "statement";
     return this;
 });
 
 stmt("debugger", function() {
     if (token.id === ";") {
         advance(";");
     }
     this.arity = "statement";
     return this;
 });

 stmt("return", function () {
     this.first = null;
     this.second = null;
     if (token.id !== ";") {
         this.first = expression(0);
     }
     if (token.id === ";") {
         advance(";");
     }
     this.arity = "statement";
     return this;
 });
 
 stmt("throw", function() {
     this.first = expression(0);
     if (token.id === ";") {
         advance(";");
     }
     this.arity = "statement";
     return this;
 });
 
 stmt("delete", function() {
     this.first = expression(0);
     if (token.id === ";") {
         advance(";");
     }
     this.arity = "statement";
     return this;
 });

 stmt("break", function () {
     if (token.id === ";") {
         advance(";");
     }
     if (token.id !== "}" && token.id !== "case" && token.id !== "default" && token.id !== "return") {
         //token.error("Unreachable statement.");
     }
     this.arity = "statement";
     return this;
 });

 stmt("while", function () {
     advance("(");
     this.first = expression(0);
     advance(")");
     if (token.value==="{") {
         this.second = block();
     } else {
         this.second = statement();
     }
     this.arity = "statement";
     return this;
 });
 
 stmt("switch", function() {
     advance("(");
     this.first = expression(0);
     advance(")");
     this.second = block();
     this.arity = "statement";
     return this;
 });
 
 stmt("case", function() {
     this.first = expression(0);
     advance(":");
     this.arity = "statement";
     return this;
 });
 
 stmt("default", function() {
     advance(":");
     this.arity = "statement";
     return this;
 });
 
 stmt("for", function() {
     this.first = [ ];
     advance("(");
     if (token.value==="var") {
         this.first.push(statement());
     } else if (token.value!==";") {
         while (token.id!==";" && token.id!==")") {
             this.first.push(expression(0));
         }
         if (token.id===";") {
             advance(";");
         }
     } else {
         advance(";");
     }
     while (token.id!==")") {
         if (token.value!==";") {
             this.first.push(expression(0));
             if (token.id===";") {
                 advance(";");
             }
         } else {
             advance(";");
         }
     }
     advance(")");
     if (token.value==="{") {
         this.second = block();
     } else {
         this.second = statement();
     }
     this.arity = "statement";
     return this;
 });

 return function (source) {
     tokens = source.tokens('=<>!+-*&|/%^', '=<>&|+-/');
     
    // this.TTT = tokens[115].value;
     // this.TTT = debug;
     //this.TTT = tokens.length;
     
     token_nr = 0;
     new_scope();
     advance();
     var s = statements();
     advance("(end)");
     scope.pop();
     if (s.length) {
         s[s.length-1].comments = nextComments;
     } else {
         s.comments = nextComments;
     }
     return s;	// tree 를 리턴해줘야해!!
 };
};



/////
//complexity.js
//@2010, Noah Peters
//http://code.google.com/p/jsmeter
//version : 0.2.7
/*
//jsmeter : 
//Written by: Noah Peters
//
Copyright (c) 2008, 2009, 2010 Noah Peters
All rights reserved.
Redistribution and use in source and binary forms, with or without 
modification, are permitted provided that the following conditions 
are met:
	-	Redistributions of source code must retain the above 
	copyright notice, this list of conditions and the following 
	disclaimer.
	-	Redistributions in binary form must reproduce the above 
	copyright notice, this list of conditions and the following 
	disclaimer in the documentation and/or other materials provided 
	with the distribution.
	-	Neither the name of the <ORGANIZATION> nor the names of 
	its contributors may be used to endorse or promote products 
	derived from this software without specific prior written 
	permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.
//
*/
/*jslint browser: true, evil: true */
/*global tree */
JSMETER.make_complexity = function() {
 var f = null,
 
     pn = null, //previous node
     
     cn = null, //current node
     
     fns = [],
 
     itself = {},
     
     sigDig = function(value, sd) {
         if (isNaN(value)) {
             return null;
         }
         if (value===0) {
             return 0;
         }
         var z = Math.ceil(Math.log(value)/Math.log(10)),
             v = Math.round(value * Math.pow(10, 0-z+sd)),
             l = (""  + v).length;
         v = v * Math.pow(10, z-sd);
         if (l-z<sd) {
             if (isNaN(v)) {
                 return null;
             }
             v = (""+v).substr(0,sd+1);
             if (v.indexOf(".")<0) {
                 v = v + ".";
             }
             while (v.length<sd+1) {
                 v = v + "0";
             }
         } else if (l<=sd) {
             if (isNaN(v)) {
                 return null;
             }
             v = (""+v).substr(0,l-z+2);
             if (v.indexOf(".")<0) {
                 v = v + ".";
             }
             while (v.length<l-z+2) {
                 v = v + "0";
             }
         }
         if (v.indexOf(".")===v.length-1) {
             v = v.replace(".", "");
         }
         return v;
     },
     
     func = function(name, parent) {
     
         this.edges = 1;
         this.nodes = 2; //entry, exit
         this.exits = 1;
         this.c = 0;
         this.complexityF = function() {
             return this.edges - this.nodes + this.exits + 1;
         };
         this.shortName = name || ("(Anonymous" + (parent.anons+=1) + ")");
         if (this.shortName[this.shortName.length-1]===".") {
             this.shortName = this.shortName + "(Anonymous" + (parent.anons+=1) + ")";
         }
         this.name = ((parent?(parent.name + "."):"") + this.shortName).replace("..",".");
         this.s = 0; //statements
         this.b = 0; //branches
         this.parent = parent;
         this.anons = 0; //anonymous function number
         this.depth = parent ? (parent.depth + 1) : 0;
         this.blockDepth = 0;
         this.lineStart = cn?cn.line:1;
         this.comments = 0;
         this.lineEnd = this.lineStart;
         this.operatorCount = 0;
         this.operandCount = 0;
         this.operators = { };
         this.operands = { };
         this.ins = 0;
         this.halsteadVocabularyF = function() {
             var h1 = 0, h2 = 0, o;
             for (o in this.operators) {
                 if (this.operators[o]===true) {
                     h1++;
                 }
             }
             for (o in this.operands) {
                 if (this.operands[o]===true) {
                     h2++;
                 }
             }
             return h1 + h2 + 4;
         };
         this.halsteadLengthF = function() {
             return this.operatorCount + this.operandCount;
         };
         this.halsteadVolumeF = function() {
             if (this.halsteadLengthF() === 0) {
                 return sigDig(0, 3);
             }
             return sigDig(Math.log(this.halsteadVocabularyF()) / Math.LN2 * this.halsteadLengthF(), 3);
         };
         this.halsteadPotentialF = function() {
             if (this.halsteadLengthF() === 0) {
                 return sigDig(0, 3);
             }
             return sigDig(Math.log(3 + this.ins) / Math.LN2 * (3 + this.ins), 3);
         };
         this.halsteadLevelF = function() {
             if (this.halsteadLengthF() === 0) {
                 return sigDig(0, 3);
             }
             return sigDig(this.halsteadPotentialF() / this.halsteadVolumeF(), 3);
         };
         this.linesF = function() {
             return this.lineEnd - this.lineStart + 1;
         };
         this.miF = function() {
             return sigDig(171 - 3.42 * Math.log(this.halsteadVolumeF()) - 0.23 * this.complexityF() - 16.2 * Math.log(this.linesF()) + 0.99 * this.comments, 5);
         };
     
     },
     
     assembleNm = function(n) {
         var nm;
         if (n.arity==="binary") {
             nm = assembleNm(n.first) + (n.value||"") + assembleNm(n.second);
         } else {
             nm = n.value||"";
         }
         return nm!=="this"?nm:"";
     },
     
     isOperator = function(n) {
         if (n.arity === "unary" ||
             n.arity === "binary" ||
             n.arity === "ternary" ||
             isBranch(n) ||
             isBlock(n) ||
             isExit(n)) {
                 return true;   
         }
         return false;
     },
     
     isOperand = function(n) {
         return !(isOperator(n)) && n.arity!=="comment";
     },
     
     isBranch = function(n) {
         //returns true if this node causes branching
         return ["if", "else", "case", "default", "catch", "finally", "?",
             "||", "&&"].indexOf(n.id)>=0;
     },
     
     isExit = function(n) {
         //return true if this node can cause the current function to exit
         return n.value==="return" || 
                 n.value==="thorw" ||
                 n.value==="exit";
     },
     
     isBlock = function(n) {
         //returns true if this node increases the depth of the function
         return n.value==="if" ||
                 n.value==="else" ||
                 n.value==="case" ||
                 n.value==="for";
     },
     
     extendName = function(n) {
         var nn = "";
         if (n.arity==="name" && n.value) {
             nn = nn + n.value;
         }
         if (n.value==="=" && n.first && n.first.arity==="name" && n.first.value) {
             nn = nn + n.first.value;
         }
         if (n.value==="{" && n.key) {
             nn = nn + n.key;
         }
         return nn;
     },
     
     arityStatementAdjustment = function(n) {
         var s = {    "unary" : 1,
                     "binary" : 2,
                     "ternary" : 3
             }[n.arity];
         return isNaN(s) ? 0 : s;
     },
     
     nodeCommentCount = function(n) {
         return n.comments ? n.comments.length : 0;
     },
     
     node = function(n, nn, depth) {
         
         var i;
         var ff;
         var nm = "";
         
         if (!n) {
             return;
         } else if (n instanceof Array) {
             //f.s+=1;
             for (i=0;i<n.length;i++) {
                 node(n[i], nn, depth);
             }
             return;
         }
         
         //window.status = "line: " + n.line + " depth: " + depth;
         
         f.blockDepth = Math.max(f.blockDepth, depth-f.depth);
         
         f.s+=1;
         
         pn = cn;
         cn = n;
         nn = nn ? (nn[nn.length-1]==="." ? nn : (nn + ".")) : "";
         
         f.s-=arityStatementAdjustment(n);
     
         f.lineEnd = n.line; 
     
         //check to see if we should start a new function
         if (n.arity==="function" && (!pn || pn.arity!=="name")) {
         //if (n.arity==="function" && (depth === 0)) {
             f = new func(nn + (n.key || n.name || ""), f);
             f.comments += nodeCommentCount(n);
             fns.push(f);
             f.ins = n.first.length;
             node(n.second, nn, depth+1);
             f.parent.s += f.s + 1;
             f.parent.lineEnd = f.lineEnd;
             f.parent.comments += f.comments;
             f = f.parent;
             return;
         } else if (n.second && n.second.arity === "function") {
             f.s+=1;
             i=0;
             nm = assembleNm(n.first);
             f = new func(nn + nm, f);
             f.comments += nodeCommentCount(n);
             fns.push(f);
             f.ins = n.second.first.length;
             node(n.second.second, nn, depth+1);
             f.parent.s += f.s + 1;
             f.parent.lineEnd = f.lineEnd;
             f.parent.comments += f.comments;
             f = f.parent;
             return;
         }
         
         //if this node does not begin a new function, measure
         f.comments += nodeCommentCount(n);
         nn = nn + extendName(n);
         
         if (isOperator(n)) {
             f.operatorCount++;
             f.operators[n.value]=true;
         } else if (isOperand(n)){
             f.operandCount++;
             f.operands[n.value]=true;
         }
         
         if (isBranch(n)) {
             //if this node causes branching then,
                 //increment the nodes in the control flow graph
                 f.nodes++;  
                 
                 //from the new node in the control flow graph, add two edges
                 f.edges+=2; 
                 
                 //increment the count of branches
                 f.b+=1;
                 
                 //f.c+=1;
         }
         
         if (isExit(n)) {
             //if this node can cause the program to exit the current function
                 //increment the number of exits
                 f.exits+=1;
         }
         
         if (isBlock(n)) {
             //if this node starts a nested depth then
                 //increment the current depth
                 depth++;    
                 
                 //check nested nodes
                 node(n.first, nn, depth);
                 node(n.second, nn, depth);
                 node(n.third, nn, depth);
                 
                 //after checking child nodes from the parse tree, 
                 //decrement the depth
                 depth--;
         } else {
         
             //check nested nodes
             node(n.first, nn, depth);
             node(n.second, nn, depth);
             node(n.third, nn, depth);
         
         }
     
     };
 
 itself.reset = function() {
 
     f = null;
     fns = [];
 
 };
     
 itself.complexity = function (tree, file) {
     f = null;
     pn = null; //previous node
     cn = null; //current node
     fns = [];
     
     var n;
     this.reset();
     this.tree = tree;
     f = new func("[[" + file + "]]");
     fns.push(f);
     if (tree instanceof Array) {
         for (n in tree) {
             if (!Array[n] && !Array.prototype[n]) {
                 cn = null;
                 node(tree[n], null, 0);
             }
         }
     } else {
         cn = null;
         node(tree, null, 0);
     }
     
 };
 
 itself.getFunctions = function() {
     return fns;
 };
 
 itself.renderStats = function(req, mode) {
     var i;
     var comp, mi, pl;
     
     var d = {
         write : function(t) {
             if (t===0) {
                 t = "0";
             }
             t = t || "";
             if (typeof t !== "string") {
                 t = t.toString();
             }
             req.write(t);
         }
     };
     
     if (mode === "JSON") {
         for (i in fns) {
             if (!Array[i] && !Array.prototype[i]) {
                 fns[i].complexity = fns[i].complexityF();
                 fns[i].mi = fns[i].miF();
                 fns[i].halsteadLevel = fns[i].halsteadLevelF();
                 fns[i].lines = fns[i].linesF();
                 fns[i].commentPct = fns[i].comments / fns[i].lines;
                 fns[i].halsteadVolume = fns[i].halsteadVolumeF();
                 fns[i].halsteadPotential = fns[i].halsteadPotentialF();
             }
         }                
         d.write(JSON.stringify(fns, null, 4));
         return;
     }
     
     d.write("<table border=\"0\" >");
     d.write("<tr>");
     d.write("<th>");
     d.write("Line");
     d.write("</th>");
     d.write("<th>");
     d.write("Function");
     d.write("</th>");
     d.write("<th>");
     d.write("Statements");
     d.write("</th>");
     d.write("<th>");
     d.write("Lines");
     d.write("</th>");
     d.write("<th>");
     d.write("Comment Lines");
     d.write("</th>");
     d.write("<th>");
     d.write("Comment%");
     d.write("</th>");
     d.write("<th>");
     d.write("Branches");
     d.write("</th>");
     d.write("<th>");
     d.write("Depth");
     d.write("</th>");
     d.write("<th>");
     d.write("Cyclomatic Complexity");
     d.write("</th>");
     d.write("<th>");
     d.write("Halstead Volume");
     d.write("</th>");
     d.write("<th>");
     d.write("Halstead Potential");
     d.write("</th>");
     d.write("<th>");
     d.write("Program Level");
     d.write("</th>");
     d.write("<th>");
     d.write("MI");
     d.write("</th>");
     d.write("</tr>");
     for (i in fns) {
         if (!Array[i] && !Array.prototype[i]) {
             comp = fns[i].complexityF();
             mi = fns[i].miF();
             pl = fns[i].halsteadLevelF();
             
             d.write("<tr>");
             d.write("<td>");
             d.write(fns[i].lineStart);
             d.write("</td>");
             d.write("<td>");
             d.write(fns[i].name.replace("[[code]].", ""));
             d.write("</td>");
             d.write("<td>");
             d.write(fns[i].s);
             d.write("</td>");
             d.write("<td>");
             d.write(fns[i].linesF());
             d.write("</td>");
             d.write("<td>");
             d.write(fns[i].comments);
             d.write("</td>");
             d.write("<td>");
             d.write(Math.round(fns[i].comments / (fns[i].linesF()) * 10000)/100 + "%");
             d.write("</td>");
             d.write("<td>");
             d.write(fns[i].b);
             d.write("</td>");
             d.write("<td>");
             d.write(fns[i].blockDepth);
             d.write("</td>");
             d.write("<td " + (comp>11?"style=\"color:red\"":"") + ">");
             d.write(comp);
             d.write("</td>");
             d.write("<td>");
             d.write(fns[i].halsteadVolumeF());
             d.write("</td>");
             d.write("<td>");
             d.write(fns[i].halsteadPotentialF());
             d.write("</td>");
             d.write("<td " + (pl<0.01?"style=\"color:red\"":"") + ">");
             d.write(pl);
             d.write("</td>");
             d.write("<td " + (mi<100?"style=\"color:red\"":"") + ">");
             d.write(mi);
             d.write("</td>");
             d.write("</tr>");
         }
     }
     d.write("</table>");
     
     /*
     d.write("<pre>" + JSON.stringify(tree, ['name', 'message', 'from', 'to', 'line', 'key',
             'value', 'arity', 'first', 'second', 'third', 'fourth', 'comments'], 4) + "</pre>");
     */
 };
     
 return itself;
 
};
JSMETER.setup();