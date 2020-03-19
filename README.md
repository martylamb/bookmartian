# bookmartian
A tag-based link database that makes it easy to collect and curate hundreds or thousands of bookmarks across devices. Whether running in the cloud or self-hosted, Bookmartian is easiest to get up and running in a docker container.

# Where to get it?
Download the latest stable build...

# How to use it?
## Add bookmarks
You can add a bookmark to bookmartian directly using the '+' button on the right side of the tab list. Fill out the form and hit 'Save'. It's easy. While you are browsing the web, you may also want a quick way to add the current page to bookmartian and for that you can [use the bookmarklet](http://localhost/settings#bookmarklet).

## Basic Search
The simplest way to search the bookmartian is through tags. By default, when you type a word (or multiple words) in the search box and hit <ENTER> bookmartian will search for bookmarks that have been tagged with all of the tags you typed.

For example, searching for "code" will return all of the bookmarks tagged with the word "code". Searching for "code javascript" will return all bookmarks tagged with both "code" AND "javascript".

## Advanced Search
More advanced queries can be constructed by adding one or more of the following query terms to the search.

### examples
visited-since:1w as:recent-visits by:most-recently-visited
returns a query named "recent-visits" with bookmarks visited within the last week, sorted with the most recently visited at the top.

code visited-since:1w by:most-visited
returns bookmarks tagged with "code" that were visited within the last week, sorted with the most frequently visited at the top.

by:most-recently-created limit:10
returns the last ten bookmarks created.

by:most-recently-created limit:10 programming
of the last ten bookmarks created, the ones tagged "programming".

### reference [](#reference)

| queryterm:modifier            | description |
| ----------------------------- | ----------- |
| `[!][tagged:]TAG`             | Default basic search term. Results will be limited to only those bookmarks tagged as TAG. The query term "tagged:" is optional. TAG is any word or space-separated list of words. |
| `as:NAME`                     | Query name. Result set will be returned with the provided NAME. |
| `by:SORT-EXPR`                | Sort modifier. Results will be sorted as indicated by the SORT-EXPR: _least-recently-created, least-recently-modified, least-recently-visited, most-recently-created, most-recently-modified, most-recently-visited, least-visited, most-visited, title,_ or _url_ |
| `[!]is:META-EXPR`             | Filter on specific metadata attributes of your bookmarks. Use the "is" query term and a META-EXPR to only return those bookmarks that meet certain other conditions. META-EXPR: _any (all sites), insecure/unsecure (non-https sites), secure (https sites) tagged (bookmarks with assigned tags), untagged (bookmarks without assigned tags)_ |
| `limit:N`                     | Restrict the number of results returned. |
| `[!]site:SITE`                | Search by hostname and subdomain |
| `[!]created-before:DATE-EXPR`  `[!]created-since:DATE-EXPR`  `[!]created[-on]:DATE-EXPR` | Filter on date created. The expression may be an explicit date or a relative date such as "1y3m" meaning "1 year 3 months". DATE-EXPR: _yyyy/MM/dd, today, yesterday, #y, #m, #w,_ or _#d_ |
| `[!]modified-before:DATE-EXPR`  `[!]modified-since:DATE-EXPR`  `[!]modified[-on]:DATE-EXPR` | Filter on date edited. |
| `[!]visited-before:DATE-EXPR`  `[!]visited-since:DATE-EXPR`  `[!]visited[-on]:DATE-EXPR` | Filter on date visited. |
| `[!]visits-over:N`  `[!]visits-under:N`  `[!]visits:N` | Filter on number of times visited. |

## Installation
Pull the latest build as a docker container from ___. The application will build the bookmarks database in a /data folder. If that directory is not present the application will happily create this folder in the container itself. It is strongly recommended, however, that you mount a persistent volume at /data so that all of your bookmarks are stored in a place that won't get deleted when the container is stopped.

# API Reference

# Developer Notes
Bookmartian is built using a Java back-end hosting the ui and api on a Jetty Spark server and a single-page Vue application on the front-end.