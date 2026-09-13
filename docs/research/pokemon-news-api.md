# Is there a usable Pokémon news API or feed?

Research for [issue #4](https://github.com/NicolasZurbuchen/pokedex/issues/4) — "Is there a usable Pokemon news API?"

- **Date of investigation:** 2026-09-12 (all HTTP probes run that day, UTC)
- **Context:** the Pokédex app is otherwise fully offline. The home screen has one networked feature: a news strip showing recent Pokémon news as *headline + date + thumbnail*. Free, non-monetised hobby/portfolio app.
- **Method:** every endpoint below was actually requested (curl with a browser User-Agent, plus `WebFetch` and a real browser where curl was blocked), not taken from documentation. Status codes and content types are the ones observed.

---

## 1. Bottom line

**There is no official Pokémon news API, and no official Pokémon RSS/Atom feed for written news.** The Pokémon Company publishes news only as HTML on `www.pokemon.com`, which is behind Imperva bot protection and whose Terms of Use prohibit exactly what this feature would do.

Three options actually survive scrutiny. In order of how defensible they are:

| Rank | Option | Headline | Date | Thumbnail | Key needed | Licensing risk |
|---|---|---|---|---|---|---|
| **A** | Official Pokémon **YouTube** channel Atom feed | yes | yes (ISO 8601) | yes, hotlinkable | none for RSS (or free YouTube Data API key) | **Low** |
| **B** | **press.pokemon.com** (TPCi official press site), scraped | yes | yes | yes, hotlinkable | none | **Medium** — an explicit non-commercial editorial licence exists, but has sharp edges |
| **C** | Ship **no live news**; curate a bundled/static list | — | — | — | — | **None** |

Everything else — pokemon.com scraping, Serebii, Pokémon GO Hub, Bulbanews, Google News RSS, NewsAPI and the other aggregators — is either technically blocked, legally prohibited, or both. Details below.

---

## 2. Official and semi-official sources

### 2.1 www.pokemon.com — news pages

**Endpoints tried (2026-09-12):**

| URL | Result |
|---|---|
| `https://www.pokemon.com/us/pokemon-news` | 200 `text/html` 316,516 B — **but intermittently** 200 with 6,058 B |
| `https://www.pokemon.com/us/news` (canonical, per `<link rel="alternate" hrefLang="en-US">`) | same behaviour |
| `https://www.pokemon.com/us/feed.xml` | 404 |
| `https://www.pokemon.com/us/rss.xml` | 404 |
| `https://www.pokemon.com/api/pokemon-news` | 200 — but the body is a bot-challenge page, not an API |
| `https://www.pokemon.com/robots.txt` | returns an HTML page, not a robots file |
| `https://www.pokemon.com/sitemap.xml`, `/us/sitemap.xml` | 200 — bot-challenge page |

**Bot protection.** The 6,058-byte responses are an Imperva/Incapsula interstitial. Verbatim from the body:

```html
<noscript><title>Pardon Our Interruption</title></noscript>
...
<script src="/_Incapsula_Resource?SWJIYLWA=...&ns=1&cb=..."></script>
```

Response headers carry `X-Iinfo:` and `Set-Cookie: visid_incap_2884021=…`. In a run of four sequential requests with a shared cookie jar, two succeeded and two were challenged; a later run of four was challenged four times out of four. **A client app cannot rely on this.**

**No feed.** The served HTML contains no `<link rel="alternate" type="application/rss+xml">` — the only `rel="alternate"` tags are `hrefLang` locale variants. There is no `__NEXT_DATA__` blob; the site is Next.js App Router (`self.__next_f`, `/_assets/_next/static/chunks/...`), so the only structured data is an RSC flight payload, which is an undocumented internal format that changes with every deploy.

**What is there, if you scrape the HTML:** article links (`/us/news/<slug>`), titles, and Cloudinary-hosted images on `mcdn.pokemon.com`. Sample:

```
https://mcdn.pokemon.com/image/upload/c_limit,w_1439/f_auto/q_auto:best/v1/live/pcom-cms/
  static-assets/cms3/us/img/video-games/tiles/pokemon-go/2026/09/08/pokemon-go-169.png
```

**Licensing — this is the blocker.** From the [Pokémon.com Terms of Use](https://www.pokemon.com/us/legal/terms-of-use), Section 5 "User Rights and Restrictions":

> "These Terms grant permission to you, in your individual capacity, to use the content of Service made available to you for **personal, noncommercial home use only**."

and among the prohibited acts:

> "(ii) Modify, or create derivative works based on, the content"
> "(iii) Use, or facilitate the use of, any unauthorized third-party software (**e.g. bots, mods, hacks, and scripts**) to modify or automate operation within the Service"
> "(v) **Download quantities of content to a database for any reason**"
> "(ix) Use the Service or content for commercial purposes…"

Clauses (iii) and (v) directly describe "an app that fetches the news list on a schedule". The permission grant is to *you, in your individual capacity, for personal home use* — it does not extend to redistributing the content to your app's users. Being free and non-monetised does not help here: (v) has no commercial qualifier at all ("for any reason").

**Verdict: do not scrape www.pokemon.com.** Technically flaky, contractually prohibited.

---

### 2.2 press.pokemon.com — TPCi official press site

This is the most interesting finding. `https://press.pokemon.com/` is The Pokémon Company International's official North America press site (hosted on the Games Press platform). It is **not** behind Imperva — plain curl gets 200 `text/html`, 60,777 B.

**robots.txt** ([https://press.pokemon.com/robots.txt](https://press.pokemon.com/robots.txt), 200 `text/plain`, 171 B) — verbatim:

```
User-agent: *
Crawl-Delay: 5
# don't crawl /Files/File
Disallow: /Files/File
Disallow: /Files/Download
Disallow: /User/CheckPayment
Disallow: /User/PaymentPending
```

Crawling the release listing is explicitly allowed, at 5 s between requests.

**No structured feed.** All of these 404: `/feed`, `/rss`, `/en/feed`, `/en/rss`, `/en/releases?format=rss`, `/wp-json/wp/v2/posts`. There is no `__NEXT_DATA__` and no WordPress/Contentful/Prezly signature. **HTML scraping is the only option.**

**The listing page does contain everything the feature needs**, in plain server-rendered HTML:

- Headline: `<a href="/en/releases/Pokemon-Celebrates-30-Years-with-New-Announcements-at-Inaugural-Pokemo">`
- Date: `<span class="date">9/9/2026 8:00 AM</span>` (US format, no timezone stated — parse defensively)
- Thumbnail: `<img src="https://imguscdn.gamespress.com/cdn/files/PokemonAmerica/2026/08/211951-.../Pokemon_XP_x_Pokemon_World_Championships_logo.png?w=240&mode=max&…">`
- Individual release pages carry `<meta property="og:title">`

**Licensing — there IS an explicit licence**, at [https://press.pokemon.com/en/Assets-Use-Terms](https://press.pokemon.com/en/Assets-Use-Terms) ("The Pokémon Company International, Inc. — Media Usage Guidelines"). Verbatim:

> **Limited license:** "The Content is intended to be used solely for editorial or informational purposes, including on blogs, newspapers, news websites, journals, and other media. TPCi grants you a non-exclusive limited right and license to use the Content, solely for informational or editorial purposes. In no event are you authorized to commercialize the Content… The license granted in these Guidelines is limited strictly to non-commercial uses. You may not sublicense or transfer this license to any third party. **You may not modify, alter or create derivative works of the Content.**"

> **Restrictions:** "You may not use the Content (i) in a way that implies partnership, sponsorship, or endorsement; (ii) in a way that makes our brand or logo the most distinctive or prominent feature of what you're creating; or (iii) in conjunction with any materials considered to be… objectionable, illegal, or otherwise offensive."

> **Brand usage:** "You may not use the Branding Material: (i) **in the name of your business, product, service, app, domain name**, social media account, or other offering or publication…"

> **Attribution:** "You shall not add any watermarks to any Content or display your own or any third-party copyright or trademark notice in connection with the Content, unless approved in advance in writing by TPCi."

> **Ownership:** "Nothing above shall be construed as granting anything more than a **temporary revocable license**…"

**Honest reading of the risk:**

- ✅ A free, non-monetised app displaying press-release headlines, dates and images for informational purposes is squarely inside "editorial or informational purposes… blogs… and other media", non-commercial.
- ⚠️ "You may not modify, alter or create derivative works of the Content." A news card that **crops or letterboxes** the press image to fit is arguably an alteration. Render images un-cropped (`fit`/`contain`, not `crop`), and don't overlay text on them.
- ⚠️ **Brand usage (i) is already a problem for this project independent of the news feature**: an app called "Pokédex" uses TPCi Branding Material in the name of the app. That is a pre-existing portfolio-project risk, not one the news section creates, but it means you are not starting from a clean slate.
- ⚠️ The licence is "temporary revocable" and the terms note they override the Games Press Ltd. terms. No stability guarantee.
- ⚠️ Scraping HTML from a site with no contract for machine access: `robots.txt` permits it, the Media Usage Guidelines permit the *use*, but the page structure has no stability contract and will break without warning.

**Verdict: the only official source with an actual written licence that arguably covers this use.** Medium risk, mostly on the "no derivative works" and app-naming clauses.

---

### 2.3 Serebii

| URL | Result |
|---|---|
| `https://www.serebii.net/index.shtml` / `index2.shtml` | 200 `text/html; charset=ISO-8859-1`, 128,463 B |
| `https://www.serebii.net/rss.xml` | 404 |
| `https://www.serebii.net/feed.xml` | 404 |
| `https://www.serebii.net/posts.xml` | 404 |
| `https://www.serebii.net/index.rss` | 404 |
| `https://feeds.feedburner.com/Serebiinet` | connection failed (curl exit, no response) |

The homepage HTML contains **zero** occurrences of `rss`, `atom`, `feed.xml` or `.rss`. **Serebii has no feed and no API.**

**robots.txt** — verbatim, complete:

```
User-agent: *
Disallow: /hidden/ranch/
Disallow: /crossword/
```

Crawling is technically permitted.

**Licensing.** Footer, verbatim: *"All Content is © Copyright of Serebii.net 1999-2026. | … Pokémon and All Respective Names are Trademark & © of Nintendo 1996-2026"*. There is no reuse policy, no permissions page, no licence. **All rights reserved by default.** Displaying Serebii headlines and images in a third-party app would require written permission from Joe Merrick.

**Verdict: no.** No feed to consume, and no licence to consume it under.

---

### 2.4 Pokémon GO Hub

`https://pokemongohub.net/feed/` is the conventional WordPress feed path, but it is **unreachable from any non-browser client**. Verified with three independent clients on 2026-09-12:

- curl with full browser headers (UA, Accept, Accept-Language, Sec-Fetch-*): **403**, body `<title>Just a moment...</title>` + Cloudflare challenge script
- `WebFetch`: **403 Forbidden**
- A real browser session, navigated and left for 14 s across two attempts: stuck on *"Performing security verification — This website uses a security service to protect against malicious bots."*

`https://pokemongohub.net/robots.txt` is likewise **403** with the same Cloudflare managed-challenge page (`_cf_chl_opt`, `cType: 'managed'`, `cZone: 'pokemongohub.net'`).

This is a **deliberate, site-wide bot block**. An Android/iOS HTTP client (OkHttp/Ktor) presents a TLS fingerprint no more browser-like than curl's, so it would be challenged too. Getting past it would mean circumventing a bot-protection measure — not something to build a shipped feature on.

Their terms could not be fetched for the same reason. Search results indicate the GO Hub Database terms state that Pokémon material is "used on the site for educational purposes only" and that the operator (Solver d.o.o.) has no affiliation with TPC/Niantic/Nintendo — i.e. they themselves rely on a fair-use posture and are in no position to sublicense.

**Verdict: no.** Hard-blocked, and no licence.

---

### 2.5 Bulbanews

Also behind Cloudflare for direct clients: `https://bulbanews.bulbagarden.net/robots.txt` and the MediaWiki feed URLs return **403** with the `Just a moment...` challenge to curl. `WebFetch` *did* get through, so the block is not absolute — but it is not something to depend on from a mobile client.

**Feeds exist but were empty when tested.** MediaWiki standard feed endpoints:

- `https://bulbanews.bulbagarden.net/w/index.php?title=Special:RecentChanges&feed=atom` → valid Atom, title *"Bulbanews - Recent changes [en]"*, **0 `<entry>` elements**
- `https://bulbanews.bulbagarden.net/w/index.php?title=Special:NewPages&feed=atom` → valid Atom, title *"Bulbanews - New pages [en]"*, **0 `<entry>` elements**

Both were well-formed but carried no items at the time of testing. Note also that these are *wiki-change* feeds, not article feeds — `RecentChanges` would surface edits and maintenance activity, not just published stories, and neither feed carries any image/thumbnail element.

**Licensing — the best of any third-party source.** Per [Bulbanews:Copyrights](https://bulbanews.bulbagarden.net/wiki/Bulbanews:Copyrights), original Bulbanews content is **Creative Commons Attribution-NonCommercial-ShareAlike 2.5 (CC BY-NC-SA 2.5)**:

> "You may copy and modify Bulbanews content, provided you attribute it to Bulbanews… provided it is not for commercial purposes… provided the resulting work is also licensed under the same license."

Content published before 2006-02-27 is not explicitly licensed, and editorials/columns may carry different licences.

**Caveats:**
- ⚠️ **ShareAlike is viral.** Displaying CC BY-NC-SA headlines/excerpts in an app raises the question of whether the app's presentation is a derivative that must also be BY-NC-SA. For an open-source portfolio app this is survivable; it is still a licence obligation you'd have to honour and document.
- ⚠️ **The images are not CC.** Bulbanews illustrations are overwhelmingly Nintendo/TPC copyrighted material used under a fair-use rationale. The CC licence covers Bulbanews' *own* content only, and cannot sublicense Nintendo's artwork to you. **So the thumbnail half of the feature is not licensed even here** — and the feeds carry no images anyway.

**Verdict: licensing is the friendliest available, but the feeds are empty of articles, carry no thumbnails, and the site is Cloudflare-gated.** Not usable for this feature as specified.

---

### 2.6 Nintendo newsroom

| URL | Result |
|---|---|
| `https://www.nintendo.com/whatsnew/` → `/us/whatsnew/` | 200 `text/html`, 354,775 B |
| `https://www.nintendo.com/whatsnew/rss` | 404 |
| `https://www.nintendo.com/feed/news.xml` | 404 |
| `https://www.pokemon.co.jp/info/rss.xml` | 404 |

The served HTML contains **no** `<link rel="alternate" type="application/rss+xml">`. **Nintendo of America publishes no RSS feed for its newsroom.**

It *is* a Next.js Pages Router site with a `__NEXT_DATA__` blob, so the internal data route works:

```
GET https://www.nintendo.com/_next/data/j5aOLr6S-iX6lNvbN_zYt/us/whatsnew.json
→ 200 application/json, 98,834 B
```

But: (a) the `buildId` path segment (`j5aOLr6S-iX6lNvbN_zYt`) changes on every deploy, so it must be re-scraped from the HTML each time; (b) inspecting the payload, it contains **navigation chrome and merchandising modules, not the article list** — zero `/us/whatsnew/<slug>` URLs appear in it, so the article list is fetched client-side from somewhere else.

`https://www.nintendo.com/robots.txt` allows general crawling (`User-agent: * / Allow: /`) while blocking a list of AI/scraper bots (CCBot, Bytespider, AI2Bot, PetalBot, etc.).

**Verdict: no.** No feed, no usable structured endpoint, and the content is general Nintendo news that would need heavy filtering to find the Pokémon items anyway.

---

### 2.7 Official Pokémon YouTube channel — **the one that actually works**

The Pokémon Company runs `youtube.com/@pokemon`. Resolved from the channel page HTML: **channel ID `UCFctpiB_Hnlk3ejWfHqSm6Q`**.

```
GET https://www.youtube.com/feeds/videos.xml?channel_id=UCFctpiB_Hnlk3ejWfHqSm6Q
→ 200 text/xml; charset=UTF-8, 25,199 B, 15 <entry> elements
```

No API key. No bot challenge. Verified twice, hours apart. Sample entry (abridged, verbatim):

```xml
<entry>
  <id>yt:video:Nm_16NxmlIY</id>
  <yt:videoId>Nm_16NxmlIY</yt:videoId>
  <title>Grumpy Snubbull | Pokémon: The Johto Journeys | Official Clip</title>
  <link rel="alternate" href="https://www.youtube.com/watch?v=Nm_16NxmlIY"/>
  <author><name>The Official Pokémon YouTube channel</name></author>
  <published>2026-09-12T10:02:58+00:00</published>
  <updated>2026-09-12T10:08:51+00:00</updated>
  <media:group>
    <media:title>Grumpy Snubbull | …</media:title>
    <media:thumbnail url="https://i3.ytimg.com/vi/Nm_16NxmlIY/hqdefault.jpg" width="480" height="360"/>
    <media:description>…</media:description>
  </media:group>
</entry>
```

That is **headline + ISO 8601 date + thumbnail URL + description**, exactly the shape the feature needs, from The Pokémon Company's own first-party channel.

**Thumbnail hotlinking verified:**
```
GET https://i3.ytimg.com/vi/Nm_16NxmlIY/hqdefault.jpg
  no Referer      → 200 image/jpeg  18,387 B
  Referer: example.com → 200        18,387 B
```
No hotlink protection. **No proxy needed.**

**Licensing.** Two routes, with different legal footing:

1. **The `videos.xml` feed** is a public RSS endpoint outside the YouTube Data API. It has no explicit terms of its own and is governed by the YouTube Terms of Service. This is the pragmatic route but sits in a grey area.
2. **YouTube Data API v3** is the properly licensed route, and the quota makes it free in practice. From the official [quota cost table](https://developers.google.com/youtube/v3/determine_quota_cost), verbatim:
   - `channels.list` → **1** unit
   - `playlistItems.list` → **1** unit
   - `videos.list` → **1** unit
   - `search.list` → *"100 quota per day. Each call costs 1 quota."*

   Default project quota is **10,000 units/day**. Fetching the channel's uploads playlist costs `channels.list` (1) + `playlistItems.list` (1) = **2 units per refresh** — ~5,000 refreshes/day within the free quota. An API key is required (free, from Google Cloud Console).

   Relevant obligations from the [YouTube API Services Developer Policies](https://developers.google.com/youtube/terms/developer-policies):
   > **III.E.4.d** — an API Client "may temporarily store limited amounts of Non-Authorized Data… but not longer than **30 calendar days**. After 30 calendar days, the API Client must either delete or refresh the stored data."
   >
   > **III.E.2.a** — "Do not aggregate API Data except that you may only aggregate API Data relating to YouTube channels that are **under the same content owner**."
   >
   > **III.C.3** — "API Clients must not modify user-provided values… by truncating, appending, or otherwise altering those values."

   All three are satisfiable: a single official channel (so the aggregation rule is fine), a cache TTL under 30 days, and displaying titles unmodified. Standard YouTube practice additionally requires that thumbnails link to the YouTube watch page and that video playback happen in the YouTube player — so the news card should open `https://www.youtube.com/watch?v=<id>`, not embed the video bare.

⚠️ **Caveat on fit:** this gives you *video* news (trailers, announcements, animation clips), not written articles. For a "recent Pokémon news" strip, official trailers and announcement videos are arguably *better* signal than blog posts — and the thumbnails are far better art than most news thumbnails. But it is a change of what the section means.

**Verdict: the recommended option.** Free, key-optional, first-party, reliable, hotlinkable thumbnails, and a real licence you can comply with.

---

## 3. Generic news aggregator APIs

All were probed live. Each returned an authentication error, which confirms the endpoint is up and that **registration + an API key is mandatory** for every one:

| API | Probe result (no key) |
|---|---|
| NewsAPI.org | `GET https://newsapi.org/v2/everything?q=pokemon` → **401** |
| GNews.io | `GET https://gnews.io/api/v4/search?q=pokemon` → **400** |
| NewsData.io | `GET https://newsdata.io/api/1/news?q=pokemon` → **401** `{"message":"The API Key is missing…"}` |
| CurrentsAPI | `GET https://api.currentsapi.services/v1/search?keywords=pokemon` → **401** |
| mediastack | `GET https://api.mediastack.com/v1/news?keywords=pokemon` → **401** `missing_access_key` |
| TheNewsAPI | `GET https://api.thenewsapi.com/v1/news/all?search=pokemon` → **401** `invalid_api_token` |

**Free-tier terms, from each vendor's own pricing page:**

**NewsAPI.org** ([pricing](https://newsapi.org/pricing)) — Developer plan:
> "100 requests per day", "No extra requests available", "Articles have a **24 hour delay**", "CORS enabled for **localhost**", and per the FAQ the plan "**cannot be used in a staging or production environment (including internally)**."

**This disqualifies NewsAPI outright.** A shipped app — even a free one — is a production environment. The 24-hour delay also makes "recent news" a lie.

**GNews.io** ([pricing](https://gnews.io/pricing)) — Free plan:
> "100 requests per day", "Up to 10 articles returned per request", "**12-hour delay**", and "the free subscription cannot be used for commercial projects. The free plan is designed for **non-commercial projects, development, and testing** purposes only."

Non-commercial is satisfied here, but 100 req/day is a **global** budget shared across every install of the app, not per-user. With a key embedded in the client, a handful of users exhausts it before lunch.

**NewsData.io** ([pricing](https://newsdata.io/pricing)) — Free plan:
> "$0/month", "API Credits **200/day**", "Articles Per credit **1 - 10**", "**Delayed by 12 hours**"

Best free quota of the group, still a shared 200/day.

**The structural problem with all of them.** Beyond the individual limits, three issues kill this category for a client-side mobile app:

1. **The key ships inside the APK/IPA.** Any user can extract it; any abuse burns *your* quota and can get *your* key revoked. Doing it properly means standing up a backend proxy — which contradicts "otherwise fully offline app" and adds hosting cost and an uptime dependency to a portfolio project.
2. **Quotas are per-key, not per-user.** 100–200 requests/day across an entire userbase is not a product, it's a demo.
3. **Aggregators don't solve the licensing question, they relocate it.** These services index third-party publishers. Their terms let *you* call *their* API; they do not grant you the publishers' copyright in headlines, excerpts and images. Several return `image_url` fields pointing at publisher CDNs, which you would then be hotlinking without any relationship with the publisher.

**Verdict: no.** NewsAPI is contractually forbidden; the rest are quota-infeasible for a distributed client and don't clear the licensing hurdle.

---

## 4. Google News RSS — explicitly prohibited

Technically this is the most appealing option: no key, no block, huge coverage.

```
GET https://news.google.com/rss/search?q=pokemon&hl=en-US&gl=US&ceid=US:en
→ 200 application/xml; charset=utf-8, 130,995 B, 104 <item> elements
```

**It is disqualified by its own payload.** The feed carries this `<copyright>` element, verbatim:

> "Copyright © 2026 Google. All rights reserved. This XML feed is made available solely for the purpose of rendering Google News results within a **personal feed reader for personal, non-commercial use**. **Any other use of the feed is expressly prohibited.** By accessing this feed or using these results in any manner whatsoever, you agree to be bound by the foregoing restrictions."

A Pokédex app's home screen is not a personal feed reader. "Any other use… is expressly prohibited" leaves no room for a favourable reading, and "non-commercial" does not rescue it — the restriction is to *personal feed readers*, not to non-commercial uses generally.

**It also fails technically.** Verified on the live payload:
- **Zero** `<media:content>`, `<media:thumbnail>` or `<enclosure>` elements across all 104 items — **no thumbnails at all**.
- **Zero** `<img` tags inside `<description>` — the description is just an anchor tag repeating the title.
- `<link>` values are opaque Google redirect URLs (`https://news.google.com/rss/articles/CBMiwwFBVV95cUxQ…?oc=5`), not publisher URLs.
- Titles are suffixed with the publisher name (`"… - The Hollywood Reporter"`), needing cleanup.

So even setting the licence aside, it cannot satisfy the thumbnail requirement.

**Verdict: no, on both counts.**

---

## 5. Third-party gaming news feeds (would need Pokémon filtering)

These all work technically and carry thumbnails. They are listed for completeness; each has a licensing problem.

| Feed | Result | Items | Thumbnails |
|---|---|---|---|
| `https://www.nintendolife.com/feeds/latest` | 200 `application/rss+xml`, 36,673 B | 25 | 25 `<media:content>` |
| `https://nintendoeverything.com/feed/` | 200 `application/rss+xml`, 21,554 B | 10 | 10 `media:content` + 10 `media:thumbnail` |
| `https://www.eurogamer.net/feed` | 200 `application/rss+xml`, 170,118 B | — | 100 `<media:content>` |
| `https://www.polygon.com/rss/index.xml` | 200 `application/xml`, 19,179 B | — | — |
| `https://www.reddit.com/r/pokemon/new.rss` | 200 `application/atom+xml`, 44,927 B | — | minimal |

**Thumbnail hotlinking verified** for Nintendo Life:
```
GET https://images.nintendolife.com/4907d0e087859/large.jpg
  no Referer → 200 image/jpeg 75,648 B
  Referer: example.com → 200  75,648 B
```
No hotlink protection. Eurogamer serves from `assetsio.gnwcdn.com` with transform params.

**Licensing — Nintendo Life** ([terms](https://www.nintendolife.com/terms), Hookshot Media Ltd), verbatim:

> "you may print or download to a local hard disk extracts for your **personal and non-commercial use only**"
> "you may copy the content to individual third parties for their personal use, but only if you acknowledge the website as the source of the material"
> "You may not, except with our express written permission, distribute or commercially exploit the content."
> "**Nor may you transmit it or store it in any other website or other form of electronic retrieval system.**"

That last sentence is fatal. Caching their headlines and thumbnails in an app and serving them to users is precisely "store it in any other… form of electronic retrieval system". Non-commercial does not exempt it — the clause is unqualified.

The other outlets' terms follow the same template (personal use, no redistribution, no storage in another system). I did not fetch each one individually because the Nintendo Life text is representative of standard UK/US publisher boilerplate and the conclusion doesn't change.

**Also note:** all of these are *general Nintendo/gaming* outlets. Filtering to Pokémon means keyword-matching titles, which is noisy — and the items you'd surface are third-party commentary, not Pokémon news, which is a weaker fit for the home screen of a Pokédex.

**Verdict: no.** Technically the easiest path, legally the one with an explicit prohibition against exactly this.

---

## 6. Thumbnail hotlinking summary

Every image host tested serves images to any client with **no Referer check and no hotlink protection**, so no image proxy is needed on technical grounds for any of them:

| Host | Source | No Referer | Foreign Referer |
|---|---|---|---|
| `i3.ytimg.com` | YouTube | 200, 18,387 B | 200, 18,387 B |
| `mcdn.pokemon.com` | pokemon.com | 200, 398,713 B | 200, 398,713 B |
| `imguscdn.gamespress.com` | press.pokemon.com | 200, 22,480 B | 200, 22,480 B |
| `images.nintendolife.com` | Nintendo Life | 200, 75,648 B | 200, 75,648 B |

⚠️ **"Can be hotlinked" ≠ "may be hotlinked."** The absence of technical protection says nothing about permission. For `mcdn.pokemon.com` and `images.nintendolife.com` the respective terms prohibit the use regardless. A proxy would be needed only to *hide* that fact, which is the wrong reason to build one.

One real technical note: `mcdn.pokemon.com` served a **398 KB** image for a news tile. If any pokemon.com-derived route were taken, the Cloudinary transform params (`c_limit,w_1439`) would need lowering — but see §2.1, that route is closed anyway.

---

## 7. Recommendation

**Option A — retarget the section to official Pokémon video (recommended).**

Consume `https://www.youtube.com/feeds/videos.xml?channel_id=UCFctpiB_Hnlk3ejWfHqSm6Q`, or the YouTube Data API v3 (`channels.list` + `playlistItems.list`, 2 quota units per refresh against a 10,000/day free allowance) for the properly-licensed version. Render title + `published` date + `media:thumbnail`, card taps out to the YouTube watch URL. Cache under 30 days per policy III.E.4.d.

- Only option that is first-party, key-free (or free-key), unblocked, thumbnail-bearing, and covered by a licence you can actually comply with.
- Relabel the section honestly — *"Latest from Pokémon"* or *"Official videos"*, not *"News"*.
- Costs: it's videos, not articles.

**Option B — scrape press.pokemon.com under the Media Usage Guidelines.**

Genuine press releases with headline, date and image, from TPCi itself, under an explicit non-commercial editorial licence, with a `robots.txt` that permits crawling at 5 s intervals.

- Honour `Crawl-Delay: 5`; cache aggressively; the listing updates a few times a month, so poll daily at most.
- Render images **un-cropped and unmodified** — "you may not modify, alter or create derivative works of the Content."
- Do not overlay your own branding or copyright on the images.
- Accept that the HTML structure has no stability contract and will break eventually, and that the licence is "temporary revocable".
- Note the pre-existing app-naming issue under the Brand usage clause.

**Option C — drop the network dependency.**

Ship a curated, bundled list of Pokémon milestones/links, or replace the section with something offline and genuinely useful (a "Pokémon of the day" card, a random-team generator, a type-matchup tip). The app's whole selling point is being fully offline; one flaky, legally-grey network call is a poor trade for a strip of headlines.

**Do not:** scrape `www.pokemon.com`, use Google News RSS, use NewsAPI's free tier in a shipped app, or republish Serebii / Pokémon GO Hub / Nintendo Life content. The first four are contractually prohibited in plain language; the last two are additionally hard-blocked by bot protection.

---

## 8. Sources

Primary sources consulted, all fetched 2026-09-12:

- [Pokémon.com Terms of Use](https://www.pokemon.com/us/legal/terms-of-use) — Section 5, User Rights and Restrictions
- [The Pokémon Company International — Media Usage Guidelines](https://press.pokemon.com/en/Assets-Use-Terms)
- [press.pokemon.com/robots.txt](https://press.pokemon.com/robots.txt)
- [serebii.net/robots.txt](https://www.serebii.net/robots.txt) and serebii.net footer copyright notice
- [Bulbanews:Copyrights](https://bulbanews.bulbagarden.net/wiki/Bulbanews:Copyrights)
- [nintendo.com/robots.txt](https://www.nintendo.com/robots.txt)
- [YouTube Data API v3 — quota costs](https://developers.google.com/youtube/v3/determine_quota_cost)
- [YouTube API Services — Developer Policies](https://developers.google.com/youtube/terms/developer-policies)
- Google News RSS `<copyright>` element, read directly from the live feed payload
- [NewsAPI.org pricing](https://newsapi.org/pricing)
- [GNews.io pricing](https://gnews.io/pricing)
- [NewsData.io pricing](https://newsdata.io/pricing)
- [Nintendo Life Terms of Use](https://www.nintendolife.com/terms) (Hookshot Media Ltd)

### Known gaps / caveats

- `www.pokemon.com` Terms of Use and the Bulbanews/Nintendo Life terms were retrieved via `WebFetch` because `www.pokemon.com` blocks direct clients via Imperva and the Bulbagarden/Hookshot pages sit behind Cloudflare. The quotes above are as returned by that fetch; they were not independently re-read from raw HTML.
- **Pokémon GO Hub's terms could not be read at all** — every client was Cloudflare-challenged. Its characterisation above rests on search-result snippets, not the primary document. Treat it as unverified.
- Bulbanews feeds returned **zero entries** on the day of testing. Whether that is a quiet wiki, a caching artefact or a disabled feature was not determined; re-test before writing the source off on activity grounds (the licensing and thumbnail conclusions stand regardless).
- Aggregator response *shapes* (whether `image_url` is populated for Pokémon queries, actual coverage depth) could not be verified, since every one requires a key. Limits and restrictions quoted are from vendor pricing pages, not from live responses.
