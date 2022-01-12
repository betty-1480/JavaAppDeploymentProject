module com.udacity.catpoint.image {
    exports com.udacity.catpoint.image.service;
    requires org.slf4j;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.regions;
    requires java.desktop;
    requires software.amazon.awssdk.services.rekognition;
}