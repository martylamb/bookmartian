bookmartian API
===============

All examples use for the [httpie client](https://github.com/jkbrzt/httpie)
and assume a server running on localhost:4567.

Bookmark JSON format
--------------------
  * url (required): url of the bookmark
  * title (optional): descriptive title of bookmarked resource
  * notes (optional): descriptive notes re bookmarked resource
  * imageUrl (optional): url for image representing bookmarked resource
  * tags (optional): array of tags describing bookmarked resource
  * created (required): timestamp of bookmark creation
  * modified (required): timestamp of last bookmark modification
  * lastVisited (optional): timestamp of last visit to bookmark via api
  * visitCount (required): number of times bookmark was visited via api

Tag JSON format
---------------
  * name (required): short name of tag.  May only include characters in [a-zA-Z0-9_.-]
  * color (required): color that should be used when rendering the tag as 6-digit hex RGB beginning with "#".  For example, black is "#000000".

Add or Update a Bookmark
------------------------

**POST /api/bookmark/update**

Parameters:

  * url (required): the url of the bookmark to add or update
  * oldUrl (optional): replace the bookmark for a different url (used if a bookmark
    is being edited and the url itself has been changed)
  * title (optional)
  * imageUrl (optional)
  * notes (optional)
  * tags: space-and-comma-delimited list of tags
  

Examples:

```shell
# add a bookmark
http -v -f POST http://127.0.0.1:4567/api/bookmark/update url=http://martiansoftware.com "tags=programming personal" imageUrl=http://martiansoftware.com/greenlogo.png "notes=my website"

# update a bookmark
http -v -f POST http://127.0.0.1:4567/api/bookmark/update oldUrl=http://martiansoftware.com url=http://www.martiansoftware.com "tags=programming personal" imageUrl=http://martiansoftware.com/greenlogo.png "notes=my website"
```


Retrieve a Single Bookmark
--------------------------

**GET /api/bookmark**

Parameters:
  * url (required): url of bookmark to retrieve
  
Example:

```shell
# get bookmark for foo.com
http -v GET http://127.0.0.1:4567/api/bookmarks "url==http://foo.com"
```


  
Query Bookmarks
---------------

**GET /api/bookmarks**

Parameters:
  * tags (optional): space-and-comma-delimited list of tags and queries.  Only 
    bookmarks with all of the specified tags and matching all of the speified
    queries will be returned.  If no tags or queries are specified then all
    bookmarks will be returned.  Sample queries include:
    * is:untagged
    * site:microsoft.com
    * is:recent
    * created-after:2016/09/07
    * created-before:2016/09/10
    * last-visited-after:2015/12/31
    * last-visited-before:2015/12/31
 

    
Examples:

```shell
# get all bookmarks
http -v GET http://127.0.0.1:4567/api/bookmarks

# get all bookmarks that match all of a set of tags
http -v GET http://127.0.0.1:4567/api/bookmarks "q==personal programming"

# get all unread bookmarks with "java" tag created in the last week
http -v GET http://127.0.0.1:4567/api/bookmarks "q==java,is:recent"
```


Visit a Bookmark
----------------

**GET /api/visit**

Parameters:
  * url (required): url of the bookmark to visit.  Must be a bookmarked url or
    else result will be 404.

Using this api rather than just linking the url directly allows bookmartian to
track the last visit time and number of times each bookmark is visited.

It is recommended to add rel="noreferrer" to the link so that the site you
visit is not informed of your bookmartian's existence or location.



Delete a Bookmark
-----------------

**POST /api/bookmarks/delete**

Parameters:
  * url (required): url of the bookmark to delete
  
Example:

```shell
# delete a bookmark
http -v -f POST http://127.0.0.1:4567/api/bookmark/delete url=http://www.martiansoftware.com
```

Get all Tags
------------

**GET /api/tags**

Parameters: none

Example:

```shell
# get all tags
http -v GET http://127.0.0.1:4567/api/tags
```

Add or Update a Tag
------------------------

**POST /api/tag/update**

Parameters:

  * name (required): the name of the tag to add or update
  * color (optional): the color that should be used when rendering the tag
  

Examples:

```shell
# add a new tag that will be colored red
http -v -f POST http://127.0.0.1:4567/api/tag/update name=newtag "color=#ff0000"

# change it to blue
http -v -f POST http://127.0.0.1:4567/api/tag/update name=newtag "color=#0000ff"
```

Delete a Tag
------------

**POST /api/tag/delete**

Parameters:
  * name (required): the name of the tag to delete
  
This will delete the tag and remove it from all bookmarks that use it.

Example:

```shell
# delete a tag
http -v -f POST http://127.0.0.1:4567/api/tag/delete name=oldtag
```

Merge Tags
----------

**POST /api/tag/merge**

Parameters:
  * target (required): the name of the tag that other tags will be merged into.  Will be created automatically if it does not exist.
  * tags (required): a space-and-comma-delimited set of tags that will be merged into the target tag. 

After this operation, all bookmarks using any of the marged tags will be tagged with the target, and none of these tags will exist in the database or in any bookmark.   
 
```shell
# merge tags
http -v -f POST http://127.0.0.1:4567/api/tag/merge target=colors "tags=red,green,blue"
```

Backup (Export) Bookmarks
----------

**GET /api/backup**

Parameters:
  * n/a

Returns a json document with all bookmarks and tags.   
 
```shell
# merge tags
http -v -f GET http://127.0.0.1:4567/api/backup
```

Restore (Import) Bookmarks
----------

**POST /api/backup**

Parameters:
  * FILE Upload 'backup'

Accepts a FILE upload json backup and imports it into the current database.
 
```shell
# merge tags
http -v -f POST http://127.0.0.1:4567/api/restore backup@/path/to/backup.json 
```
