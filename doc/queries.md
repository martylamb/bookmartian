Bookmark Queries
================

Bookmarks may be searched, sorted, and limited based upon a user-specified
search string.  Multiple query terms may be specified in a single query.
If multiple query terms are provided, they are evaluated left-to-right
in the order specified.

An empty query returns all Bookmarks.

Query Terms
-----------
Each query term consists of an "action" and an "argument".  If the action
is omitted, the default action of "tagged" is used.  The action (if 
specified) and argument are separated by a colon.  Actions are 
case-insensitive.

The following actions are supported:

  * `tagged:TAG` or `TAG` - results will be limited to only those
     bookmarks tagged as `TAG`.
  
  * `is:untagged` - results will be limited to only those bookmarks with
     no tags.
     
  * `site:foo.com` - results will be limited to only those bookmarks with
    urls hosted at foo.com or its subdomains (e.g. searching for "foo.com"
    will match bookmarks for "foo.com", "www.foo.com", and "www.bar.foo.com").
    
  * `visit-count:EXPR` - results will be limited to only those bookmarks
    with a visit count that matches the specified expression.  The expression
    may be a number (in which case only those bookmarks visited exactly
    that number of times will be included in results) or may start with
    a comparison operator: ">", ">=", "<", or "<=" followed immediately
    by a number, in which case only the results depend upon the operator
    and the following number.  For convenience, both "=" and "==" are
    supported and are equivalent to specifying no operator at all.  For
    example, `visit-count:10` will limit results to only those bookmarks
    visited exactly ten times, while `visit-count:>=10` will instead
    limit results to those bookmarks visited ten times or more.
    
  * `created:EXPR` - results will be limited to only those bookmarks with
    a creation time that matches the specified expression.  The expression
    may be a date in the format "yyyy/mm/dd", or a comparison operator
    (as described above for `visit-count`) followed immediately by a date.
    For example, `created:2016/09/17` will limit results to only those
    created on September 17, 2016, whereas `created:>=2016/09/17` will
    limit results to those created either on or after that date.
    
  * `last-visited:EXPR` - same as `created:EXPR` but using the bookmark's
    last-visited time.
    
  * `last-modified:EXPR` - same as `created:EXPR` but using the bookmark's
    last-modified time.  A modification is an edit of any of the bookmark's
    properties, including its tags.

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
    
Examples
--------

Bookmarks with both "programming" and "web" tags:  `programming web`

The last ten bookmarks created: `by:most-recently-created limit:10`

The last ten "programming" bookmarks created: `programming by:most-recently-created limit:10`

Of the last ten bookmarks created, the ones tagged "programming": `by:most-recently-created limit:10 programming`
    
