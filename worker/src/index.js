const ALLOWED_HOST = "www.mojnovisad.com";

const CORS_HEADERS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET",
};

export default {
  async fetch(request) {
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

    const pageResponse = await fetch(eventUrl, {
      headers: { "User-Agent": "Mozilla/5.0 (compatible; WhatsHappeningBot/1.0)" },
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

    return new Response(JSON.stringify(result), {
      headers: { ...CORS_HEADERS, "Content-Type": "application/json" },
    });
  },
};

function jsonError(message, status) {
  return new Response(JSON.stringify({ error: message }), {
    status,
    headers: { ...CORS_HEADERS, "Content-Type": "application/json" },
  });
}
