package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class ParallelWebCrawler implements WebCrawler {

  private final Clock clock;
  private final Duration timeout;
  private final int popularWordCount;
  private final ForkJoinPool pool;

  private final PageParserFactory parserFactory;
  private final int maxDepth;

  private final List<Pattern> ignoredUrls;
  private final List<String> ignoredWords;

  @Inject
  ParallelWebCrawler(
      Clock clock,
      @Timeout Duration timeout,
      @PopularWordCount int popularWordCount,
      @TargetParallelism int threadCount,
      PageParserFactory parserFactory,
      @MaxDepth int maxDepth,
      @IgnoredUrls List<Pattern> ignoredUrls,
      List<String> ignoredWords) {

    this.clock = clock;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.parserFactory = parserFactory;
    this.maxDepth = maxDepth;

    this.pool = new ForkJoinPool(
        Math.min(threadCount, getMaxParallelism()));

    this.ignoredUrls = ignoredUrls;
    this.ignoredWords = ignoredWords;
  }

  @Override
  public CrawlResult crawl(List<String> startingUrls) {

    Instant deadline = clock.instant().plus(timeout);

    Set<String> visitedUrls = new ConcurrentSkipListSet<>();
    Map<String, Integer> counts = new LinkedHashMap<>();

    for (String url : startingUrls) {
      pool.invoke(new CrawlTask(
          url,
          deadline,
          visitedUrls,
          counts,
          0,
          clock,
          parserFactory,
          maxDepth,
          ignoredUrls,
          ignoredWords));
    }

    Map<String, Integer> sorted =
        counts.entrySet()
            .stream()
            .sorted(
                Map.Entry.<String, Integer>comparingByValue()
                    .reversed()
                    .thenComparing(Map.Entry.comparingByKey()))
            .limit(popularWordCount)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (a, b) -> a,
                LinkedHashMap::new));

    return new CrawlResult.Builder()
        .setWordCounts(sorted)
        .setUrlsVisited(visitedUrls.size())
        .build();
  }

  @Override
  public int getMaxParallelism() {
    return Runtime.getRuntime().availableProcessors();
  }
}
