// ==========================================================================
// API reference calls
var API_VisitLink = "/api/visit";
var API_QueryBookmarks = "/api/bookmarks";
var API_UpdateBookmark = "/api/bookmark/update";
var API_DeleteBookmark = "/api/bookmark/delete";
var API_QueryTags = "/api/tags";

// ==========================================================================
// construct an HTML table row string to visualize a single bookmark and its edit/delete/detail capabilities
function buildBookmarkRow(element, withTags) {
    
    var padlock = ""
    if (element.url.substr(0, 5) === "https") {
        padlock = "<span style='color:green;'>&#x1f512;</span> ";
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
        tags = " - <span class='tags secondary-text-color'>" + element.tags.toString().replace(/,/g, ', ') + "</span>"
    }

    var lastVisited = "";
    var lastVisitedTime = "";
    if (typeof element.lastVisited != 'undefined') {
        var lastVisitedDate = new Date(element.lastVisited);
        // if the bookmark was visited in the last 8 hours, also display the time of the visit
        if (Date.now()-lastVisitedDate < 28800000) {
            lastVisitedTime = " at " + lastVisitedDate.toLocaleTimeString() + " ";
        }
        lastVisited = "visited on " + lastVisitedDate.toDateString() ;
    }

    var created = "";
    if (typeof element.created != 'undefined') {
        var createdDate = new Date(element.created);
        created = "created on " + createdDate.toDateString() ;
    }

    var modified = "";
    if (typeof element.modified != 'undefined') {
        var modifiedDate = new Date(element.modified);
        modified = "modified on " + modifiedDate.toDateString() ;
    }

    var row = "<tr><td class='favicon'><img src='http://www.google.com/s2/favicons?domain_url=" + element.url + "' onclick='toggleEdits(this);'></td><td class='bookmark'><a rel='noreferrer' href='" + API_VisitLink + "?url=" + element.url + "' data-url='" + element.url + "' data-tags='" + element.tags + "' data-notes='" + notes  + "' data-imageurl='" + imageUrl + "' data-title='" + element.title + "'>" + padlock + element.title + "</a>" + tags + "</td></tr><tr class='bookmarkedits'><td colspan=2><a onclick='editMark(this);'>edit</a> | <a onclick='deleteMark(this);'>delete</a><p class='dateinfo text-default-primary-color'>" + element.url + "</p><p class='dateinfo secondary-text-color'>" + created + "</br>" + modified + "</br>" + lastVisited + lastVisitedTime + "</p></td></tr>";

    return row;
}

// ==========================================================================
// populate link table
function populateLinkTable(value) {
    var tagColors = new Array();
    $.ajax({
        // The URL for the request
        url: API_QueryBookmarks + "?tags=" + value.replace(/\|/g, '+'),

        // Whether this is a POST or GET request
        type: "GET",

        // The type of data we expect back
        dataType: "json"
    })
        // Code to run if the request succeeds (is done);
        // The response is passed to the function
        .done(function (json) {
            var safeID = value.replace(/[\|,\.]/g, '_');
            var linktable = $('#linktable_' + safeID)

            //  linktable.children().remove();

            $(json).each(function (index, element) {
                linktable.append(buildBookmarkRow(element, false));
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

// ==========================================================================
// POST the bookmark back to the service to save it
function saveBookmark() {
    $.post(API_UpdateBookmark, $("#addform").serialize())
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
        $(e).parent().next('.bookmark').css('background-color', '#e1e1e1');
        $(e).parent().parent().next('.bookmarkedits').show();
        $(e).data('on', 1);
    } else {
        $(e).parent().next('.bookmark').css('background-color', 'white');
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
    $('#addinputurl').val(bookmark.attr('data-url') ? bookmark.attr('data-url') : '');
    $('#addinputtags').val(bookmark.attr('data-tags') ? bookmark.attr('data-tags').replace(/,/g, ' ') : '');
    $('#addinputnotes').val(bookmark.attr('data-notes') ? bookmark.attr('data-notes') : '');
    $('#addinputimageUrl').val(bookmark.attr('data-imageurl') ? bookmark.attr('data-imageurl') : '');
    $('#actionpanel').slideDown('fast');
    $('#addinputtags').focus();
}

// ==========================================================================
// run a bookmark search by sending the contents of the search box to the bookmarks service
function executeSearch(term) {
    var searchterm = "";

    if (term) {
        searchterm = term;
    } else {
        searchterm = $('#searchterm').val();
    }

    closeAction();
    $('#searchtable').children().remove();
    $('#searchresultstitle').text('search results for \'' + searchterm + '\'');

    $.ajax({
        // The URL for the request
        url: API_QueryBookmarks + "?tags=" + searchterm,

        // Whether this is a POST or GET request
        type: "GET",

        // The type of data we expect back
        dataType: "json"
    })
        // Code to run if the request succeeds (is done);
        // The response is passed to the function
        .done(function (json) {
            var searchtable = $('#searchtable');
            $(json).each(function (index, element) {
                searchtable.append(buildBookmarkRow(element, true));
            })
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
    // create promoted tag blocks from the querystring
    var qd = {};
    location.search.substr(1).split("&").forEach(function (item) { (item.split("=")[0] in qd) ? qd[item.split("=")[0]].push(item.split("=")[1]) : qd[item.split("=")[0]] = [item.split("=")[1]] })
    var promotedTags = "";
    if (qd.pins) {
        var promotedTags = qd.pins.toString();
        var promotedTagArray = promotedTags.split(",");
    }

    // create a linkblock for each pinned tag
    $.each(promotedTagArray, function (index, value) {
        var block = $("#linkblocktemplate").clone()
        block.css('display', 'inline-block');
        var heading = block.find('h1');
        var safeID = value.replace(/[\|,\.]/g, '_');
        var cleanHeading = value.replace(/\.[^|]+/g, '').replace(/\|/g, '');
        heading.text(cleanHeading);
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
                    var link = "<a onclick=\"executeSearch('" + element.name + "');\" style=color:" + element.color + " id=" + element.name + ">" + element.name + "</a>";
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
    // retrieve promo tiles
    $.ajax({
        // The URL for the request
        url: API_QueryBookmarks + "?tags=promote",

        // Whether this is a POST or GET request
        type: "GET",

        // The type of data we expect back
        dataType: "json"
    })
        // Code to run if the request succeeds (is done);
        // The response is passed to the function
        .done(function (json) {
            $(json).each(function (index, element) {
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
                link.attr("href", element.url)
                tile.css("display", "inline-block");
                $(".promotedsection").prepend(tile);
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

});
