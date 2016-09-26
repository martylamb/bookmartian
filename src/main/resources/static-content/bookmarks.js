var bookmarkJSONArrays = new Array();

// ==========================================================================
// construct an HTML table row string to visualize a single bookmark and its edit/delete/detail capabilities
function buildBookmarkRow(element, withTags) {

    var padlock = ""
    if (element.url.substr(0, 5) === "https") {
        padlock = "&nbsp;&nbsp;<i class='fa fa-lock padlock'></i>";
    }

    var notes = "";
    if (typeof element.notes != 'undefined') {
        notes = element.notes;
    }

    var imageUrl = "";
    if (typeof element.imageUrl != 'undefined') {
        imageUrl = element.imageUrl;
    }

    var tags = "";
    if (withTags) {
        var taglist = element.tags.toString().split(',');
        tags = " - "
        taglist.forEach(function (tag) {
            tags += "<span onclick=\"executeSearch('" + tag + "', false);\" class='tags secondary-text-color'>" + tag + "</span> ";
        }, this);
    }

    var lastVisited = "";
    var lastVisitedTime = "";
    if (typeof element.lastVisited != 'undefined') {
        var lastVisitedDate = new Date(element.lastVisited);
        // if the bookmark was visited in the last 8 hours, also display the time of the visit
        if (Date.now() - lastVisitedDate < 28800000) {
            lastVisitedTime = " at " + lastVisitedDate.toLocaleTimeString() + " ";
        }
        lastVisited = "visit #" + element.visitCount + " on " + lastVisitedDate.toDateString();
    }

    var created = "";
    if (typeof element.created != 'undefined') {
        var createdDate = new Date(element.created);
        created = "created on " + createdDate.toDateString();
    }

    var modified = "";
    if (typeof element.modified != 'undefined') {
        var modifiedDate = new Date(element.modified);
        modified = "modified on " + modifiedDate.toDateString();
    }

    var row = "<tr><td class='favicon'><img src='http://www.google.com/s2/favicons?domain_url=" + element.url + "'></td><td class='bookmark'><a rel='noreferrer' href='" + API_VisitLink + "?url=" + escape(element.url) + "' data-url='" + element.url + "' data-tags='" + element.tags + "' data-notes='" + notes + "' data-imageurl='" + imageUrl + "' data-title='" + element.title + "'>" + element.title + "</a>" + padlock + tags + "<i class='fa fa-angle-down edit-ellipsis tertiary-text-color' aria-hidden='true' onclick='toggleEdits(this)'></i></td></tr><tr class='bookmarkedits'><td colspan=2><a onclick='editMark(this);'>edit</a> | <a onclick='deleteMark(this);'>delete</a><p class='dateinfo text-default-primary-color'>" + element.url + "</p><p class='dateinfo secondary-text-color'>" + created + "</br>" + modified + "</br>" + lastVisited + lastVisitedTime + "</p></td></tr>";

    return row;
}

// ==========================================================================
// sort the bookmark list table
function sortTable(thisTable, sortField) {

    var listDataJSON = bookmarkJSONArrays[thisTable.attr('id')];

    // --------------------------------------------------------------------------
    // sort by title
    if (sortField === "title") {
        if (thisTable.attr('data-sort') === 'by:title_asc' || thisTable.attr('data-sort') === 'by:title') {
            listDataJSON.sort(function (a, b) {
                if (a.title == b.title) return 0;
                if (a.title < b.title) return 1;
                if (a.title > b.title) return -1;
            });
            thisTable.attr('data-sort', 'by:title_desc');
        } else {
            listDataJSON.sort(function (a, b) {
                if (a.title == b.title) return 0;
                if (a.title < b.title) return -1;
                if (a.title > b.title) return 1;
            });
            thisTable.attr('data-sort', 'by:title_asc');
        }
    }

    // --------------------------------------------------------------------------
    // sort by lastVisited    
    if (sortField === "recently-visited") {
        if (thisTable.attr('data-sort') === 'by:most-recently-visited') {
            listDataJSON.sort(function (a, b) {
                if ((typeof a.lastVisited != 'undefined')) {
                    var aDate = new Date(a.lastVisited);
                }
                else {
                    var aDate = new Date("1/1/1970");
                }
                if ((typeof b.lastVisited != 'undefined')) {
                    var bDate = new Date(b.lastVisited);
                }
                else {
                    var bDate = new Date("1/1/1970");
                }
                if (aDate == bDate) return 0;
                if (aDate < bDate) return -1;
                if (aDate > bDate) return 1;
            });
            thisTable.attr('data-sort', 'by:least-recently-visited');
        } else {
            listDataJSON.sort(function (a, b) {
                if ((typeof a.lastVisited != 'undefined')) {
                    var aDate = new Date(a.lastVisited);
                }
                else {
                    var aDate = new Date("1/1/1970");
                }
                if ((typeof b.lastVisited != 'undefined')) {
                    var bDate = new Date(b.lastVisited);
                }
                else {
                    var bDate = new Date("1/1/1970");
                }
                if (aDate == bDate) return 0;
                if (aDate < bDate) return 1;
                if (aDate > bDate) return -1;
            });
            thisTable.attr('data-sort', 'by:most-recently-visited');
        }
    }

    // --------------------------------------------------------------------------
    // sort by created    
    if (sortField === "recently-created") {
        if (thisTable.attr('data-sort') === 'by:most-recently-created') {
            listDataJSON.sort(function (a, b) {
                if ((typeof a.created != 'undefined')) {
                    var aDate = new Date(a.created);
                }
                else {
                    var aDate = new Date("1/1/1970");
                }
                if ((typeof b.created != 'undefined')) {
                    var bDate = new Date(b.created);
                }
                else {
                    var bDate = new Date("1/1/1970");
                }
                if (aDate == bDate) return 0;
                if (aDate < bDate) return -1;
                if (aDate > bDate) return 1;
            });
            thisTable.attr('data-sort', 'by:least-recently-created');
        } else {
            listDataJSON.sort(function (a, b) {
                if ((typeof a.created != 'undefined')) {
                    var aDate = new Date(a.created);
                }
                else {
                    var aDate = new Date("1/1/1970");
                }
                if ((typeof b.created != 'undefined')) {
                    var bDate = new Date(b.created);
                }
                else {
                    var bDate = new Date("1/1/1970");
                }
                if (aDate == bDate) return 0;
                if (aDate < bDate) return 1;
                if (aDate > bDate) return -1;
            });
            thisTable.attr('data-sort', 'by:most-recently-created');
        }
    }
}

// ==========================================================================
// sort the parent table
function sortThisTable(e, sortField) {
    var thisTable = $(e).parent().parent().find("table");
    sortTable(thisTable, sortField);

    // if this is the search results table, make sure that we are still displaying tags after sort
    var withTags = false;
    if (thisTable.attr('id') === 'searchtable') withTags = true;

    renderLinkTable(thisTable, withTags);
}

// ==========================================================================
// render link table
function renderLinkTable(linktable, withTags) {
    // clear the table out
    linktable.empty();

    // insert bookmark rows in the current sort order
    bookmarkJSONArrays[linktable.attr('id')].each(function (index, element) {
        linktable.append(buildBookmarkRow(element, withTags));
    })

    var sorttitlelink = linktable.parent().find('.sorttitle');
    var sortlastvisitedlink = linktable.parent().find('.sortlastvisited');
    var sortcreatedlink = linktable.parent().find('.sortcreated');

    // reset them all to normal weight
    sorttitlelink.css("font-weight", "normal");
    sorttitlelink.html("title");
    sortlastvisitedlink.css("font-weight", "normal");
    sortlastvisitedlink.html("visited");
    sortcreatedlink.css("font-weight", "normal");
    sortcreatedlink.html("created");

    // bold the active sort and insert an up or down arrow to reflect sort order
    switch (linktable.attr('data-sort')) {
        case 'by:title':
        case 'by:title_asc':
            sorttitlelink.css("font-weight", "bold");
            sorttitlelink.html("title <i class='fa fa-long-arrow-up' aria-hidden=true></i>");
            break;
        case 'by:title_desc':
            sorttitlelink.css("font-weight", "bold");
            sorttitlelink.html("title <i class='fa fa-long-arrow-down' aria-hidden=true></i>");
            break;
        case 'by:least-recently-visited':
            sortlastvisitedlink.css("font-weight", "bold");
            sortlastvisitedlink.html("visited <i class='fa fa-long-arrow-up' aria-hidden=true></i>");
            break;
        case 'by:recently-visited':
        case 'by:most-recently-visited':
            sortlastvisitedlink.css("font-weight", "bold");
            sortlastvisitedlink.html("visited <i class='fa fa-long-arrow-down' aria-hidden=true></i>");
            break;
        case 'by:least-recently-created':
            sortcreatedlink.css("font-weight", "bold");
            sortcreatedlink.html("created <i class='fa fa-long-arrow-up' aria-hidden=true></i>");
            break;
        case 'by:recently-created':
        case 'by:most-recently-created':
            sortcreatedlink.css("font-weight", "bold");
            sortcreatedlink.html("created <i class='fa fa-long-arrow-down' aria-hidden=true></i>");
            break;
    }
}

// ==========================================================================
// populate link table
function populateLinkTable(value) {
    $.ajax({
        // The URL for the request
        url: API_QueryBookmarks + "?q=" + value.replace(/\|/g, '+'),

        // Whether this is a POST or GET request
        type: "GET",

        // The type of data we expect back
        dataType: "json"
    })
        // Code to run if the request succeeds (is done);
        // The response is passed to the function
        .done(function (json) {
            var safeID = stringToIntegerHash(value);
            var linktable = $('#linktable_' + safeID)

            var heading = linktable.parent().find('h1');
            if (json.status !== 'error') {
                heading.text(json.data.name);

                bookmarkJSONArrays['linktable_' + safeID] = $(json.data.bookmarks);
                linktable.attr('data-sort', json.data.sort);
                renderLinkTable(linktable, false);
            } else {
                heading.text(json.message);
            }

        })
        // Code to run if the request fails; the raw request and
        // status codes are passed to the function
        .fail(function (xhr, status, errorThrown) {
            console.log("Error: " + errorThrown);
            console.log("Status: " + status);
            console.dir(xhr);
        })
        // Code to run regardless of success or failure;
        .always(function (xhr, status) {

        });
}

// ==========================================================================
// POST the bookmark back to the service to save it
function saveBookmark() {

    // do not serialize the oldurl field if no old url was specified or the url did not change
    var serializedFormData = null;
    if ($('#addinputoldUrl').val().length == 0 || $('#addinputoldUrl').val() === $('#addinputurl').val()) {
        serializedFormData = $("#addform :not(#addinputoldUrl)").serialize();
    } else {
        serializedFormData = $("#addform").serialize();
    }

    $.post(API_UpdateBookmark, serializedFormData)
        .done(function () {
            closeAction();
        })
        .fail(function () {
            console.log("POST failed");
        })
        .always(function (data) {
            console.log(data);
        });
}

// ==========================================================================
// close the action panel and reset the field contents
function closeAction() {
    $('#actionpanel').slideUp('fast');
    $('#addform').find("input[type=text], textarea").val("");
}

// ==========================================================================
// toggle the display of the edit section of a bookmark table row
function toggleEdits(e) {
    if (!$(e).data('on')) {
        $(e).removeClass('tertiary-text-color fa-angle-down');
        $(e).addClass('text-primary-color fa-angle-up');
        $(e).parent().removeClass('table-color');
        $(e).parent().addClass('light-primary-color');
        $(e).parent().prev('.favicon').removeClass('table-color');
        $(e).parent().prev('.favicon').addClass('light-primary-color');
        $(e).parent().parent().next('.bookmarkedits').show();
        $(e).data('on', 1);
    } else {
        $(e).removeClass('text-primary-color fa-angle-up');
        $(e).addClass('tertiary-text-color fa-angle-down');
        $(e).parent().removeClass('light-primary-color');
        $(e).parent().addClass('table-color');
        $(e).parent().prev('.favicon').removeClass('light-primary-color');
        $(e).parent().prev('.favicon').addClass('table-color');
        $(e).parent().parent().next('.bookmarkedits').hide();
        $(e).data('on', 0);
    }
}

// ==========================================================================
// POST to delete a bookmark from the service
function deleteMark(e) {
    var bookmark = $(e).parent().parent().prev().find('.bookmark a');
    console.log("deleting bookmark: " + bookmark.attr('href'));

    $.ajax({
        type: 'POST',
        url: API_DeleteBookmark,
        data: { 'url': bookmark.attr('data-url') }
    })
        .done(function () {
            console.log('delete POST successful');
            $(e).parent().remove();
            bookmark.parent().parent().remove();
        })
        .fail(function () {
            console.log("delete POST failed");
        })
        .always(function (data) {
            console.log(data);
        });
}

// ==========================================================================
// open up the action panel with the bookmark ready for editing
function editMark(e) {
    var bookmark = $(e).parent().parent().prev().find('.bookmark a');
    console.log("editing bookmark: " + bookmark.attr('href'));

    $('#addinputtitle').val(bookmark.attr('data-title') ? bookmark.attr('data-title') : '');
    $('#addinputoldUrl').val(bookmark.attr('data-url') ? bookmark.attr('data-url') : '');
    $('#addinputurl').val(bookmark.attr('data-url') ? bookmark.attr('data-url') : '');
    $('#addinputtags').val(bookmark.attr('data-tags') ? bookmark.attr('data-tags').replace(/,/g, ' ') : '');
    $('#addinputnotes').val(bookmark.attr('data-notes') ? bookmark.attr('data-notes') : '');
    $('#addinputimageUrl').val(bookmark.attr('data-imageurl') ? bookmark.attr('data-imageurl') : '');
    $('#actionpanel').slideDown('fast');
    $('#addinputtags').focus();
}

// ==========================================================================
// run a bookmark search by sending the contents of the search box to the bookmarks service
// term - tag or tags to be used in search, if null the value of the search text box is used
// reset - if true, treat this as a new search. if false, treat this as a drill-down search
function executeSearch(term, reset) {
    var searchtable = $('#searchtable');
    var searchterm = "";

    if (term) {
        searchterm = term;
        if (searchtable.attr('data-searchterm') && !reset) {
            searchterm = searchtable.attr('data-searchterm') + " " + searchterm;
        }
    } else {
        searchterm = $('#searchterm').val();
    }

    closeAction();
    $('#searchtable').children().remove();
    $('#searchresultstitle').text('search results for \'' + searchterm + '\'');

    $.ajax({
        // The URL for the request
        url: API_QueryBookmarks + "?q=" + searchterm,

        // Whether this is a POST or GET request
        type: "GET",

        // The type of data we expect back
        dataType: "json"
    })
        // Code to run if the request succeeds (is done);
        // The response is passed to the function
        .done(function (json) {
            var searchtable = $('#searchtable');

            bookmarkJSONArrays['searchtable'] = $(json.data.bookmarks);
            searchtable.attr('data-searchterm', searchterm);
            searchtable.attr('data-sort', json.data.sort);
            renderLinkTable(searchtable, true);

            $('#search').show();
            $('#searchhr').show();
        })
        // Code to run if the request fails; the raw request and
        // status codes are passed to the function
        .fail(function (xhr, status, errorThrown) {
            console.log("Error: " + errorThrown);
            console.log("Status: " + status);
            console.dir(xhr);
        })
        // Code to run regardless of success or failure;
        .always(function (xhr, status) {
        });

}

// ==========================================================================
// When the document is fully loaded, load the dynamic elements into the page
$(document).ready(function () {

    // --------------------------------------------------------------------------
    // wire an event handler to capture the search box ENTER key
    $('#searchterm').keypress(function (event) {

        var keycode = (event.keyCode ? event.keyCode : event.which);
        if (keycode == '13') {
            executeSearch();
        }

    });
    $('#searchterm').focus();

    // --------------------------------------------------------------------------
    // parse promoted queries from the querystring
    var qd = {};
    location.search.substr(1).split("&").forEach(function (item) { (item.split("=")[0] in qd) ? qd[item.split("=")[0]].push(item.split("=")[1]) : qd[item.split("=")[0]] = [item.split("=")[1]] })

    // create a linkblock for each pinned tag
    var promotedTags = "";
    if (qd.pin) {
        var promotedTags = qd.pin.toString();
        var promotedTagArray = promotedTags.split(",");
    }

    $.each(promotedTagArray, function (index, value) {
        var block = $("#linkblocktemplate").clone()
        block.css('display', 'inline-block');
        var safeID = stringToIntegerHash(value);
        var linktable = block.find('.linktable');
        linktable.attr("id", "linktable_" + safeID);
        block.appendTo('#content');
        populateLinkTable(value);
    });

    // --------------------------------------------------------------------------
    // retrieve tag cloud
    var tagColors = new Array();
    $.ajax({
        // The URL for the request
        url: API_QueryTags,

        // Whether this is a POST or GET request
        type: "GET",

        // The type of data we expect back
        dataType: "json"
    })
        // Code to run if the request succeeds (is done);
        // The response is passed to the function
        .done(function (json) {
            $(json).each(function (index, element) {
                if (element.name.substr(0, 1) !== ".") {
                    var link = "<a onclick=\"executeSearch('" + element.name + "', true);\" style=color:" + element.color + " id=" + element.name + ">" + element.name + "</a>";
                    $(".tagcloud").append(link).append(" &nbsp;");
                    tagColors[element.name] = element.color;
                }
            })
        })
        // Code to run if the request fails; the raw request and
        // status codes are passed to the function
        .fail(function (xhr, status, errorThrown) {
            console.log("Error: " + errorThrown);
            console.log("Status: " + status);
            console.dir(xhr);
        })
        // Code to run regardless of success or failure;
        .always(function (xhr, status) {
            // color the hearing of each linkblock for pinned tags
            $.each(promotedTagArray, function (index, value) {
                var cleanHeading = value.replace(/\.[^|]+/g, '').replace(/\|/g, '');
                var heading = $("#content").find("h1:contains('" + cleanHeading + "')");
                heading.css('color', tagColors[cleanHeading]);
            });
        });

    // --------------------------------------------------------------------------
    // retrieve promoted tiles based on querystring query "tiles="
    if (qd.tiles) {
        $.ajax({
            // The URL for the request
            url: API_QueryBookmarks + "?q=" + qd.tiles.toString().replace(/\|/g, '+'),

            // Whether this is a POST or GET request
            type: "GET",

            // The type of data we expect back
            dataType: "json"
        })
            // Code to run if the request succeeds (is done);
            // The response is passed to the function
            .done(function (json) {
                $(json.data.bookmarks).each(function (index, element) {
                    var tile = $("#promotedlinktemplate").clone()
                    tile.attr("title", element.title);
                    tile.attr("id", element.title);
                    var image = tile.find("img");
                    if (element.imageUrl) {
                        image.attr("src", element.imageUrl);
                    } else {
                        image.attr("src", "https://icons.better-idea.org/icon?size=90&url=" + element.url);
                    }
                    var link = tile.find("a");
                    link.attr("href", API_VisitLink + "?url=" + escape(element.url))
                    tile.css("display", "inline-block");
                    $(".promotedsection").append(tile);
                })
            })
            // Code to run if the request fails; the raw request and
            // status codes are passed to the function
            .fail(function (xhr, status, errorThrown) {
                console.log("Error: " + errorThrown);
                console.log("Status: " + status);
                console.dir(xhr);
            })
            // Code to run regardless of success or failure;
            .always(function (xhr, status) {
            });
    }
});
