# News sources

The pages below were checked on 24 November 2026 with a descriptive bot name.

| Source | Result |
|---|---|
| `www.cebu.gov.ph` (Province of Cebu) | **Used.** Plain HTML, and robots.txt allows bots. |
| `www.dpwh.gov.ph` | Not usable. Every page, including robots.txt, answers with a bot-detection page that needs JavaScript. |
| `www.mandauecity.gov.ph` | Not usable. The portal has no news list. |
| `www.cebucity.gov.ph` | Not used. robots.txt forbids bots. |
| `www.pna.gov.ph` (Philippine News Agency) | Not used. The terms of use restrict derivative works, so permission is needed. |
| `pia.gov.ph`, `www.officialgazette.gov.ph`, `www.dotr.gov.ph` | Not usable. They refuse automated requests (403). |

The bot reads three listings, which show DPWH, road and flood news of the province:

- `https://www.cebu.gov.ph/?s=DPWH`
- `https://www.cebu.gov.ph/?s=road`
- `https://www.cebu.gov.ph/?s=flood`

## How it works

1. Take up to 5 articles per listing whose title or excerpt contains an infrastructure keyword.
2. Skip articles that are already stored.
3. Read the article page for its text and keep whole sentences up to 300 characters.
4. Store the title, summary, source name, link, date and thumbnail.

A source that fails, is slow (over 10 seconds) or changes its layout is logged and skipped. The website is not affected.

## Changing sources

Edit these keys in `src/main/resources/application.properties`:

| Key | Meaning |
|---|---|
| `pa-ayo.news.source-urls` | Listing pages, separated by commas |
| `pa-ayo.news.keywords` | Words that mark infrastructure news |
| `pa-ayo.news.cron` | Daily schedule (Asia/Manila) |
| `pa-ayo.news.max-summary-length` | Maximum summary length |

Before adding a site, check its robots.txt and terms of use. Page selectors are only in `NewsScraper.java`; update the fixtures in `src/test/resources/fixtures` when you change them.
