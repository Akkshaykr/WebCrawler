package com.udacity.webcrawler;
import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

import com.udacity.webcrawler.json.CrawlResult;

import javax.inject.Inject;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ForkJoinPool;



final class ParallelWebCrawler implements WebCrawler {
  private final Clock clock;
  private final Duration timeout;
  private final int popularWordCount;
  private final ForkJoinPool pool;
  private final int maxDepth;
  private final List<Pattern> ignoredUrls;
  private final PageParserFactory parserFactory;

  @Inject
  ParallelWebCrawler(
          Clock clock,
          @Timeout Duration timeout,
          @PopularWordCount int popularWordCount,
          @TargetParallelism int threadCount,
          @MaxDepth int maxDepth,
          @IgnoredUrls List<Pattern> ignoredUrls,
          PageParserFactory parserFactory) {
    this.clock = clock;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
    this.maxDepth = maxDepth;
    this.ignoredUrls = ignoredUrls;
    this.parserFactory = parserFactory;
  }

  @Override
  public CrawlResult crawl(List<String> startingUrls) {
    Instant deadline = clock.instant().plus(timeout);
    Map<String, Integer> counts = new ConcurrentHashMap<>();
    Set<String> visitedUrls = new ConcurrentSkipListSet<>();

    for (String url : startingUrls) {
      pool.invoke(new CrawlTask(url, deadline, maxDepth, counts, visitedUrls));
    }

    if (counts.isEmpty()) {
      return new CrawlResult.Builder()
              .setWordCounts(counts)
              .setUrlsVisited(visitedUrls.size())
              .build();
    }

    return new CrawlResult.Builder()
            .setWordCounts(WordCounts.sort(counts, popularWordCount))
            .setUrlsVisited(visitedUrls.size())
            .build();
  }
    private class CrawlTask extends RecursiveAction {
      private final String url;
      private final Instant deadline;
      private final int maxDepth;
      private final Map<String, Integer> counts;
      private final Set<String> visitedUrls;

      CrawlTask(String url, Instant deadline, int maxDepth,
                Map<String, Integer> counts, Set<String> visitedUrls) {
        this.url = url;
        this.deadline = deadline;
        this.maxDepth = maxDepth;
        this.counts = counts;
        this.visitedUrls = visitedUrls;
      }

      @Override
      protected void compute() {
        // Stop if too deep or time is up
        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
          return;
        }
        // Skip ignored URLs
        for (Pattern pattern : ignoredUrls) {
          if (pattern.matcher(url).matches()) {
            return;
          }
        }
        // Skip already visited URLs
        if (!visitedUrls.add(url)) {
          return;
        }
        // Download and parse the page
        PageParser.Result result = parserFactory.get(url).parse();

        // Count words found on page
        for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
          counts.merge(e.getKey(), e.getValue(), Integer::sum);
        }

        // Create subtasks for each link — runs in PARALLEL!
        List<CrawlTask> subtasks = new ArrayList<>();
        for (String link : result.getLinks()) {
          subtasks.add(new CrawlTask(link, deadline, maxDepth - 1, counts, visitedUrls));
        }
        invokeAll(subtasks);
      }
    }


  @Override
  public int getMaxParallelism() {
    return Runtime.getRuntime().availableProcessors();
  }

}