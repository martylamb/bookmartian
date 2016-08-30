bookmartian usage
=================

Querystring
-----------

The querystring can be used to customize bookmartian's 'dashboard' display.

Parameters:

  * pins (optional): comma-delimited list of tags sets to be highlighted on the dashboard
  

Examples:

```
# display just one tag block listing all bookmarks tagged with 'news'
http://domain.com/index.html?pins=news

# display just one tag block, but only list bookmarks tagged with both 'news' and '.pin'
http://domain.com/index.html?pins=.pin|news

# display two tag blocks, the first list bookmarks tagged with both 'news' and '.pin',
#  the second lists bookmarks tagged with both 'work' and '.pin.
http://domain.com/index.html?pins=.pin|news,.pin|work
```

Tag Conventions
---------------

  * Tags prepended with a period (.) are not displayed in the tag cloud or when included in the title of a tag block (as configured on the querystring)
  * A tag set consists of two or more tags 'AND'ed together and delimited with a pipe (|) - used in the pins querystring parameter