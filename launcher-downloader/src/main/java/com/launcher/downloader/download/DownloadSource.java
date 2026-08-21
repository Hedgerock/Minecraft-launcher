package com.launcher.downloader.download;

import java.io.IOException;
import java.io.InputStream;

interface DownloadSource {

    InputStream open(String url) throws IOException;

}
