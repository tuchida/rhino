
load("testsrc/assert.js");

var a = {
  [1+2]: 'prop1',
  ['foo' + 'bar']: 'prop2',
  [a]: 'prop3',
  [{}]: 'prop4',
  [null]: 'prop5'
};
assertEquals('prop1', a[3]);
assertEquals('prop2', a.foobar);
assertEquals('prop3', a.undefined);
assertEquals('prop4', a['[object Object]']);
assertEquals('prop5', a.null);

assertThrows('var b = { []: };', SyntaxError);
assertThrows('var c = { [1, 2]: };', SyntaxError);

assertEquals('\n' +
'function () {\n' +
'    return {[a + 123]: 456};\n' +
'}\n', function () {
    return {[a + 123]: 456};
}.toString());

var d = 0;
var e = {
  [d++]: d++,
  [d++]: d++,
  [d++]: d++
};
assertEquals(1, e[0]);
assertEquals(3, e[2]);
assertEquals(5, e[4]);

var f = 'foo';
var g = {
  toString: function() {
    return f;
  }
};
var h = {
  [g]: (function() {
    f = 'bar';
    return 'abc';
  })(),
  [g]: 'efg'
};

assertEquals('abc', h.foo);
assertEquals('efg', h.bar);
/*
var i = 'abc';
var { [i]: j } = { abc: 123 };

assertEquals('123', j);

var k = {
  ['__proto__']: { abc: 123 }
};
assertEquals(undefined, k.abc);
*/
// tight swap out literals
var l = (function(n) {
  return {
    'key0': 'value0',
    'key1': 'value1',
    'key2': 'value2',
    'key3': 'value3',
    'key4': 'value4',
    'key5': 'value5',
    'key6': 'value6',
    'key7': 'value7',
    'key8': 'value8',
    'key9': 'value9',
    ['key' + n]: 'value10'
  };
})(10);

assertEquals('value10', l.key10);

"success"
