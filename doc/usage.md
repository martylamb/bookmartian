bookmartian usage
=================

Querystring
-----------

The querystring can be used to customize bookmartian's 'dashboard' display.

Parameters:

  * pin (optional, repeated): tag query of a tag block to be displayed on the page.
  * tiles (optional, once): tag query for the row of promoted tiles 
  

Examples:

```
# display just one tag block listing all bookmarks tagged with 'news'
http://domain.com/index.html?pin=news

# display just one tag block, bookmarks recently visited
http://domain.com/index.html?last-visited:>1w+as:recent-visits+by:most-recently-visited

# display just one tag block, with a promoted tiles section
http://domain.com/index.html?pin=news&tiles=promote+by:most-recently-visited

# display two tag blocks, the first list bookmarks tagged with both 'news' and 'pin',
#  the second lists bookmarks tagged with both 'work' and 'pin'.
http://domain.com/index.html?pin=pin+news&pin=pin+work
```

Tag Conventions
---------------

  * Tags prepended with a period (.) are not displayed in the tag cloud or when included in the title of a tag block (as configured on the querystring)
  * A tag set consists of two or more tags 'AND'ed together and delimited with a pipe (|) - used in the pins querystring parameter
  * Bookmarks tagged with the 'promote' tag are displayed as graphical tiles on the homepage

Bookmarklet
-----------

The bookmarklet located in the footer of the homepage can be used to customize quickly add bookmarks to the database while browsing the web.

Set-up:

  * Drag and drop the 'bookmarklet' link to the favorites/bookmark bar in your browser
  * You can rename the newly created bookmarklet button by right-clicking on the button (I suggest something like 'add a bookmark' or '+bookmark')

Usage:

  * While viewing a web page you wish to bookmark click on your bookmarklet button and an overlay window will appear at the top of the page
  * TIP: If you highlight a short block of text on the web page BEFORE clicking the bookmarklet button, the highlighted text will be automatically populated in the notes field for the new bookmark
  * Fill out the form and click the 'save' button

