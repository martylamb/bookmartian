// ==========================================================================
// API reference calls
var API_VisitLink = "/api/visit";
var API_QueryBookmarks = "/api/bookmarks";
var API_UpdateBookmark = "/api/bookmark/update";
var API_DeleteBookmark = "/api/bookmark/delete";
var API_QueryTags = "/api/tags";
var API_ImportBookmark = "/api/bookmarks/import";

// ==========================================================================
// Cookie manipulation
function setCookie(key, value) {
    var expires = new Date();
    expires.setTime(expires.getTime() + (1 * 24 * 60 * 60 * 1000));
    document.cookie = key + '=' + value + ';expires=' + expires.toUTCString();
}

function getCookie(key) {
    var keyValue = document.cookie.match('(^|;) ?' + key + '=([^;]*)(;|$)');
    return keyValue ? keyValue[2] : null;
}

// ==========================================================================
// create an integer hash from a string (used to create safe ID attributes)
function stringToIntegerHash(str) {
    var hash = 0;
    if (str.length == 0) return hash;
    for (i = 0; i < str.length; i++) {
        char = str.charCodeAt(i);
        hash = ((hash << 5) - hash) + char;
        hash = hash & hash; // Convert to 32bit integer
    }
    return hash;
}