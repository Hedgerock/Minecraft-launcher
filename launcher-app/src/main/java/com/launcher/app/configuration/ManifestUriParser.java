package com.launcher.app.configuration;

import java.net.URI;

interface ManifestUriParser {

    URI parse(String value);
    URI parseManaged(String value);
}
