module com.udacity.catpoint.security {
    requires com.udacity.catpoint.image;
    requires java.desktop;
    requires miglayout;
    requires java.prefs;
    requires com.google.common;
    requires com.google.gson;
   // opens com.udacity.catpoint.security.service;
    opens com.udacity.catpoint.security.data to com.google.gson;
}