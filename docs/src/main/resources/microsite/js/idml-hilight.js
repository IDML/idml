console.log("registering IDML highlighter");

hljs.registerLanguage("idml", function(h) {
return {
  cI: false,
  k: 'let if then else match not substr this root and or contains exists in cs true false',
  c: [
    {
      cN: 'comment',
      b: /#/, e: /\n/
    },
    {
      cN: 'string',
      b: /"/, e: /"/,
    },
    {
      cN: 'number',
      b: hljs.CNR
    },
    {
      cN: 'variable',
      v: [
        {b: /\$[a-zA-Z]+/},
        {b: /\@[a-zA-Z]+/}
      ]
    },
    {
      cN: 'title',
      b: /\[[a-zA-Z]+\]/
    },

  ],
};
});
hljs.configure({languages:['scala','java','bash', 'idml']});
hljs.initHighlighting.called = false;
hljs.initHighlighting();
