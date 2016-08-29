bookmartian API
===============

All examples use for the [httpie client](https://github.com/jkbrzt/httpie)
and assume a server running on localhost:4567.

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


Get All Bookmarks
-----------------

**GET /api/bookmarks**

Parameters:
  * tags: space-and-comma-delimited list of tags.  Only bookmarks with
    all of the specified tags will be returned.
    
Examples:

```shell
# get all bookmarks
http -v GET http://127.0.0.1:4567/api/bookmarks

# get all bookmarks that match all of a set of tags
http -v GET http://127.0.0.1:4567/api/bookmarks "tags==personal programming"
```


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
