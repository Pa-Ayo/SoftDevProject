# News fixtures

Saved HTML pages that `NewsScraper` tests parse **offline** (NFR-15). Tests must never call the real government sites.

| File | Used for |
|---|---|
| `listing-with-articles.html` | Normal listing: 3 items, 2 with thumbnails, 1 without date/image (TC-07, TC-09) |
| `article-no-date-no-thumbnail.html` | Date falls back to today, `imageUrl` stays null (TC-09) |
| `changed-layout.html` | Site redesign: parser must return an empty list, not crash (TC-10) |
| `empty-listing.html` | No results (TC-10) |

These are **invented samples** shaped like a WordPress listing. Once `NewsScraper`'s selectors are final,
save a real copy next to them so the tests cover the real markup:

```bash
curl -A "PaAyoNewsBot/1.0 (student project)" "https://www.cebu.gov.ph/?s=DPWH" \
  -o src/test/resources/fixtures/news/real-cebu-search-2026-11-DPWH.html
```

Then point a test at it with `Fixtures.document("news/real-cebu-search-2026-11-DPWH.html")`
and update the selectors in `NewsScraper` only (NFR-14).
