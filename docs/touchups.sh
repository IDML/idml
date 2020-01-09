#!/bin/sh
echo "Doing touchups"
sed -i 's:<li><a href="/user-guide/getting-started.html"><i class="fa fa-file-text"></i><span class="hidden-xs">Documentation</span></a></li>:<li><a href="/getting-started.html"><i class="fa fa-file-text"></i><span class="hidden-xs">Language Docs</span></a></li><li><a href="/basic-usage.html"><i class="fa fa-file-text"></i><span class="hidden-xs">API Docs</span></a></li>:g' docs/target/**/*.html

sed -i 's|<p class="text-center"><a href="https://github.com/idml/idml" class="btn btn-outline-inverse">View on GitHub</a></p>|<p class="text-center"><a href="https://github.com/idml/idml/releases" class="btn btn-outline-inverse">Releases</a><a href="/getting-started.html" class="btn btn-outline-inverse">Language Docs</a><a href="/basic-usage.html" class="btn btn-outline-inverse">API Docs</a></p>|g' docs/target/**/*.html
