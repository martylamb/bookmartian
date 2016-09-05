
function populateLinkTable(value) {
    // ==========================================================================
    // populate link table
    var tagColors = new Array();
    $.ajax({
        // The URL for the request
        url: "/api/bookmarks?tags=" + value.replace(/\|/g, '+'),

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

                var padlock = ""
                if (element.url.substr(0, 5) === "https") {
                    padlock = "<span style='color:green;'>&#x1f512;</span> ";
                }

                var notes = "";
                if (typeof element.notes != 'undefined') {
                    notes = element.notes;
                }
                var row = "<tr><td class='favicon'><img src='http://www.google.com/s2/favicons?domain_url=" + element.url + "' onclick='toggleEdits(this);'></td><td class='bookmark'><a href='" + element.url + "' data-tags='" + element.tags + "' data-notes='" + notes + "' data-title='" + element.title + "'>" + padlock + element.title + "</a></td></tr><tr class='bookmarkedits'><td colspan=2><a onclick='editMark(this);'>edit</a> | <a  onclick='deleteMark(this);'>delete</a></td></tr>";
                linktable.append(row);
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

function saveBookmark() {
    $.post('/api/bookmark/update', $("#addform").serialize())
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

function closeAction() {
    $('#actionpanel').slideUp('fast');
    $('#addform').find("input[type=text], textarea").val("");
}

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

function deleteMark(e) {
    var bookmark = $(e).parent().parent().prev().find('.bookmark a');
    console.log("deleting bookmark: " + bookmark.attr('href'));

    $.ajax({
        type: 'POST',
        url: '/api/bookmark/delete',
        data: { 'url': bookmark.attr('href') }
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

function editMark(e) {
    var bookmark = $(e).parent().parent().prev().find('.bookmark a');
    console.log("editing bookmark: " + bookmark.attr('href'));

    $('#addinputtitle').val(bookmark.attr('data-title') ? bookmark.attr('data-title') : '');
    $('#addinputurl').val(bookmark.attr('href') ? bookmark.attr('href') : '');
    $('#addinputtags').val(bookmark.attr('data-tags') ? bookmark.attr('data-tags').replace(/,/g, ' ') : '');
    $('#addinputnotes').val(bookmark.attr('data-notes') ? bookmark.attr('data-notes') : '');
    $('#actionpanel').slideDown('fast');
    $('#addinputtags').focus();
}

function executeSearch(term) {
    var searchterm = "";

    if (term) {
        searchterm = term;
    } else {
        searchterm = $('#searchterm').val();
    }

    closeAction();
    $('#searchtable').children().remove();

    $.ajax({
        // The URL for the request
        url: "/api/bookmarks?tags=" + searchterm,

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
                var padlock = ""
                if (element.url.substr(0, 5) === "https") {
                    padlock = "<span style='color:green;'>&#x1f512;</span> ";
                }

                var notes = "";
                if (typeof element.notes != 'undefined') {
                    notes = element.notes;
                }

                var row = "<tr><td class='favicon'><img src='http://www.google.com/s2/favicons?domain_url=" + element.url + "' onclick='toggleEdits(this);'/></td><td class='bookmark'><a href='" + element.url + "' data-tags='" + element.tags + "' data-notes='" + notes + "' data-title='" + element.title + "'>" + padlock + element.title + "</a> - <span class='tags secondary-text-color'>" + element.tags.toString().replace(/,/g, ' ') + "</span></td></tr><tr class='bookmarkedits'><td colspan=2><a onclick='editMark(this);'>edit</a> | <a  onclick='deleteMark(this);'>delete</a></td></tr>";
                searchtable.append(row);
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

    $('#searchterm').focus();
    $('#searchterm').select();
}

$(document).ready(function () {

    // ==========================================================================
    // wire an event handler to capture the search box ENTER key
    $('#searchterm').keypress(function (event) {

        var keycode = (event.keyCode ? event.keyCode : event.which);
        if (keycode == '13') {
            executeSearch();
        }

    });
    $('#searchterm').focus();

    // ==========================================================================
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

    // ==========================================================================
    // retrieve tag cloud
    var tagColors = new Array();
    $.ajax({
        // The URL for the request
        url: "/api/tags",

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

    // ==========================================================================
    // retrieve promo tiles
    $.ajax({
        // The URL for the request
        url: "/api/bookmarks?tags=.promote",

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
