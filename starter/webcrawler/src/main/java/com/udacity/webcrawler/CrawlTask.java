package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

final class CrawlTask extends RecursiveAction {

  private final String url;
  private final Instant deadline;
  private final Set<String> visitedUrls;
  private final ConcurrentMap<String, Integer> counts;
  private final int depth;

  private final Clock clock;
  private final PageParserFactory parserFactory;
  private final int maxDepth;

  private final List<Pattern> ignoredUrls;
  private final List<Pattern> ignoredWords;

  CrawlTask(
      String url,
      Instant deadline,
      Set<String> visitedUrls,
      ConcurrentMap<String, Integer> counts,
      int depth,
      Clock clock,
      PageParserFactory parserFactory,
      int maxDepth,
      List<Pattern> ignoredUrls,
      List<Pattern> ignoredWords) {

    this.url = url;
    this.deadline = deadline;
    this.visitedUrls = visitedUrls;
    this.counts = counts;
    this.depth = depth;
    this.clock = clock;
    this.parserFactory = parserFactory;
    this.maxDepth = maxDepth;
    this.ignoredUrls = ignoredUrls;
    this.ignoredWords = ignoredWords;
  }

  @Override
  protected void compute() {

    // Timeout check
    if (clock.instant().isAfter(deadline)) {
      return;
    }

    // Depth limit check
    if (depth > maxDepth) {
      return;
    }

    // Ignore URL patterns
    for (Pattern pattern : ignoredUrls) {
      if (pattern.matcher(url).matches()) {
        return;
      }
    }

    // Already visited?
    if (!visitedUrls.add(url)) {
      return;
    }

    PageParser.Result result =
        parserFactory.get(url).parse();

    // Count words
    result.getWordCounts().forEach((word, count) -> {

      for (Pattern pattern : ignoredWords) {
        if (pattern.matcher(word).matches()) {
          return;
        }
      }

      counts.merge(word, count, Integer::sum);
    });

    // Stop recursion if max depth reached
    if (depth == maxDepth) {
      return;
    }

    List<CrawlTask> subtasks = new ArrayList<>();

    for (String link : result.getLinks()) {
      CrawlTask task = new CrawlTask(
          link,
          deadline,
          visitedUrls,
          counts,
          depth + 1,
          clock,
          parserFactory,
          maxDepth,
          ignoredUrls,
          ignoredWords
      );
      task.fork();
      subtasks.add(task);
    }

    for (CrawlTask task : subtasks) {
      task.join();
    }
  }
}

