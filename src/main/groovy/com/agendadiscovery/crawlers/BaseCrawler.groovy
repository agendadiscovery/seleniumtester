package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper

abstract class BaseCrawler {
    abstract List<DocumentWrapper> getDocuments(String baseUrl);
}
