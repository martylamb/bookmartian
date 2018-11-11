var currentlyTypingTag = "";

// ==========================================================================
// When the document is fully loaded, load the dynamic elements into the page
// 
// rebuild the tag cloud at the top of the page
$(document).ready(function () {
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

            if ($("#tagcloud").data("results_hash") != stringToIntegerHash(JSON.stringify(json))) {

                // save the hash of the query results for comparison on update
                $("#tagcloud").data("results_hash", stringToIntegerHash(JSON.stringify(json)));

                $("#tagcloud").empty();
                $("#tagcloud").append("<span class='secondary-text-color'>click to add a tag &gt;&nbsp;&nbsp;&nbsp;</span>");
    
                $(json).each(function (index, element) {
                    if (element.name.substr(0, 1) !== ".") {
                        var link = "<a onclick=\"assignTag('" + element.name + "');\" class=tagflow style=color:" + (element.color != "#000000" ? element.color : "") + " id=" + element.name + "> &nbsp;" + element.name + " &nbsp;</a>";
                        $("#tagcloud").append(link);
                        tagColors[element.name] = element.color;
                    }
                })
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
    });

// ==========================================================================
// try to match tag when you are typing in the tags box
$('body').on('keypress', '#bookmartian_addinputtags', function (e) {
    // if the user types a space or other non-typable character, reset the filter
    if ((e.which < 10) || (e.which == 32)) {
        currentlyTypingTag = "";
        resetTagCloudFilter();

    // if the user presses enter, insert all visible tags into the field and reset the filter
    } else if (e.which == 13) {
        
        // insert the matching tags
        $("a[id^="+currentlyTypingTag+"].tagflow").each(function(i) {
            assignTag($(this).attr("id"));
        })

        // delete the partially typed tag
        currentTags = $("#bookmartian_addinputtags").val();
        $("#bookmartian_addinputtags").val(currentTags.replace(currentlyTypingTag + " ", ""));

        // reset the filter
        resetTagCloudFilter();
    } else {
        var c = String.fromCharCode(e.which);
        currentlyTypingTag = currentlyTypingTag + c;
        filterTagCloud(currentlyTypingTag);
    }
});

$('body').on('keydown', '#bookmartian_addinputtags', function (e) {
    // if the user types a tab, reset the filter
    if (e.which == 9) {
        resetTagCloudFilter();
    }});


function assignTag (tag) {
    currentTags = $("#bookmartian_addinputtags").val();

    // only add the tag to the list if it isn't already present
    if (!currentTags.includes(" " + tag + " ")) {
        if (currentTags.endsWith(" ")) {
            $("#bookmartian_addinputtags").val(currentTags + tag + " ");
        } else {
            $("#bookmartian_addinputtags").val(currentTags + " " + tag + " ");
        }
    }
}

function filterTagCloud(partialTag) {
    $("a.tagflow").css("color","");
    $("a").not("[id^="+partialTag+"].tagflow").css("color","lightgrey");
}

function resetTagCloudFilter() {
    currentlyTypingTag = "";
    $("a.tagflow").css("color","");
}