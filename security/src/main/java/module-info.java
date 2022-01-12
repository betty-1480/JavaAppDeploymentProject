module com.udacity.catpoint.security {
    requires java.desktop;
    requires miglayout;
    requires java.prefs;
    requires com.udacity.catpoint.image;
    requires guava;
    requires com.google.gson;
    opens com.udacity.catpoint.security.service;
    opens com.udacity.catpoint.security.data to com.google.gson;
}