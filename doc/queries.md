Bookmark Queries
================

Bookmarks may be searched, sorted, and limited based upon a user-specified
search string.  Multiple query terms may be specified in a single query.
If multiple query terms are provided, they are evaluated left-to-right
in the order specified.

An empty query returns all bookmarks.

If no ordering ("by:X") is specified, a default sort order of
most-recently-created is used.

Query Terms
-----------
Each query term consists of an "action" and an "argument".  If the action
is omitted, the default action of "tagged" is used.  The action (if 
specified) and argument are separated by a colon.  Actions are 
case-insensitive.

The following actions are supported:

  * `tagged:TAG` or `TAG` - results will be limited to only those
     bookmarks tagged as `TAG`.

  * `is:any` - returns all bookmarks
  
  * `is:tagged` - results will be limited to only those bookmarks with
    tags.
  
  * `is:untagged` - results will be limited to only those bookmarks with
     no tags.
     
  * `is:secure` - only results with secure protocol schemes (e.g. "https", "sftp") will be returned

  * `is:unsecure` - only results WITHOUT secure protocol schemes will be returned
     
  * `site:foo.com` - results will be limited to only those bookmarks with
    urls hosted at foo.com or its subdomains (e.g. searching for "foo.com"
    will match bookmarks for "foo.com", "www.foo.com", and "www.bar.foo.com").
    
  * `created-before:EXPR` results will be limited to only those bookmarks
    created before the specified expression.  The expression may be 
    an explicit date in the form yyyy/MM/dd, or a relative date such as
    "1y3m" meaning "1 year 3 months ago"  Relative dates may be specified
    using years, months, weeks, and days.  Relative dates may also be
    the fixed values "today" or "yesterday"
    
  * `created-since:EXPR` results will be limited to only those bookmarks
    created on or after the date described in EXPR.  See created-before
    for syntax.
    
  * `created:EXPR` or `created-on:EXPR` - results will be limited to
    only those bookmarks created on the the specified date.
    
  * `modified-before:EXPR`, `modified-since:EXPR`, `modified:EXPR`, `modified-on:EXPR` -
    results will be limited to only those bookmarks modified according
    to the specified expression.
    
  * `visited-before:EXPR`, `visited-since:EXPR`, `visited:EXPR`, `visiteded-on:EXPR` -
    results will be limited to only those bookmarks last visited according
    to the specified expression.
       
  * `visits-over:N`, `visits-under:N`, `visits:N` - results will be limited
    to only those bookmarks with visit counts according to the specified expression.
    
  * `by:title` - results will be sorted in ascending order by title
    (case-insensitive).
    
  * `by:url` - results will be sorted in ascending order by url (case-
    insensitive)
    
  * `by:most-recently-created` - results will be sorted with most recently
    created bookmarks listed first.
    
  * `by:least-recently-created` - results will be sorted with least recently
    created bookmarks listed first.
    
  * `by:most-recently-visited` - results will be sorted with most recently
    visited bookmarks listed first.
    
  * `by:least-recently-visited` - results will be sorted with least recently
    visited bookmarks listed first.

  * `by:most-recently-modified` - results will be sorted with most recently
    modified bookmarks listed first.
    
  * `by:least-recently-modified` - results will be sorted with least recently
    modified bookmarks listed first.
    
  * `by:most-visited` - results will be sorted with most-visited (highest
    visit-count) bookmarks listed first.
    
  * `by:least-visited` - results will be sorted with least-visited (lowest
    visit-count) bookmarks listed first.
        
  * `limit:N` - results will be limited to the first N as specified
  
  * `as:query-name` - resulting list of bookmarks will be named as
    specified by the user.  Does not modify contents of results.  At
    time of this writing, query-name may not contain whitespace.
    
    
Examples
--------

Bookmarks with both "programming" and "web" tags:  `programming web`

The last ten bookmarks created: `by:most-recently-created limit:10`

The last ten "programming" bookmarks created: `programming by:most-recently-created limit:10`

Of the last ten bookmarks created, the ones tagged "programming": `by:most-recently-created limit:10 programming`
    
