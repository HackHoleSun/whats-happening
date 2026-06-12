const ALLOWED_HOST = "www.mojnovisad.com";

// Event descriptions are effectively immutable once published, so cache
// aggressively: 1h at the edge for the extracted JSON, 1h for the origin HTML.
const CACHE_TTL_SECONDS = 3600;

const CORS_HEADERS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET",
};

export default {
  async fetch(request, env, ctx) {
    if (request.method === "OPTIONS") {
      return new Response(null, { headers: CORS_HEADERS });
    }

    const { searchParams } = new URL(request.url);
    const eventUrl = searchParams.get("url");

    if (!eventUrl) {
      return jsonError("Missing 'url' query parameter", 400);
    }

    let parsedUrl;
    try {
      parsedUrl = new URL(eventUrl);
    } catch {
      return jsonError("Invalid URL", 400);
    }

    if (parsedUrl.hostname !== ALLOWED_HOST) {
      return jsonError(`Only ${ALLOWED_HOST} URLs are allowed`, 400);
    }

    // Serve the extracted JSON from the edge cache when possible. Normalize the
    // cache key to just the event URL so param order/extra params don't fragment it.
    const cache = caches.default;
    const cacheKey = new Request(
      `https://${new URL(request.url).hostname}/?url=${encodeURIComponent(eventUrl)}`,
    );
    const cached = await cache.match(cacheKey);
    if (cached) {
      return cached;
    }

    const pageResponse = await fetch(eventUrl, {
      headers: { "User-Agent": "Mozilla/5.0 (compatible; WhatsHappeningBot/1.0)" },
      cf: { cacheTtl: CACHE_TTL_SECONDS, cacheEverything: true },
    });

    if (!pageResponse.ok) {
      return jsonError(`Failed to fetch event page: ${pageResponse.status}`, 502);
    }

    const result = { description: "", imageUrl: null };

    await new HTMLRewriter()
      .on(".single-blog__content", {
        text(chunk) {
          result.description += chunk.text;
        },
      })
      .on("img[src*='wp-content/uploads']", {
        element(el) {
          if (!result.imageUrl) {
            result.imageUrl = el.getAttribute("src");
          }
        },
      })
      .transform(pageResponse)
      .arrayBuffer();

    result.description = result.description.trim();

    const response = new Response(JSON.stringify(result), {
      headers: {
        ...CORS_HEADERS,
        "Content-Type": "application/json",
        "Cache-Control": `public, max-age=${CACHE_TTL_SECONDS}`,
      },
    });

    ctx.waitUntil(cache.put(cacheKey, response.clone()));
    return response;
  },
};

function jsonError(message, status) {
  return new Response(JSON.stringify({ error: message }), {
    status,
    headers: { ...CORS_HEADERS, "Content-Type": "application/json" },
  });
}
