You are a senior Kotlin Multiplatform library engineer. Build an open-source Kotlin Multiplatform
downloader library named “KDown”.

## Goals

- Kotlin Multiplatform (KMP): Android + iOS + JVM/desktop (at minimum Android + JVM must work; iOS
  can be “best effort” with expect/actual).
- Core features:
    1) Download with progress callbacks (bytes downloaded, total bytes, speed optional)
    2) Pause / Resume (true resume using HTTP Range; not “restart from 0”)
    3) Multi-threaded segmented downloads (N segments using Range requests)
    4) Robust cancellation
    5) Retry with exponential backoff (configurable)
    6) Persist resumable metadata (so app restart can resume)

## Constraints / Tech choices

- Use Ktor Client as the default HTTP layer in common code (preferred for KMP).
- Keep architecture pluggable: allow custom HttpEngine if needed.
- Use Kotlin coroutines; no blocking IO in common code.
- Provide expect/actual for filesystem operations (or an abstraction) so downloads can write to a
  file on Android/JVM; for iOS store to a path via platform APIs.
- Ensure thread-safety for pause/resume/cancel.

## Core behavior requirements

1) Before downloading, perform a HEAD (or GET with Range 0-0) to detect:
    - content-length (total size)
    - accept-ranges support
    - ETag / Last-Modified (used for resume validation)
2) If server does NOT support ranges, fall back to single-connection download (connections=1).
3) If ranges supported:
    - Split into segments [start..end] based on total size and connections.
    - Each segment downloads concurrently using Range: bytes=start-end.
4) Writes:
    - Use a platform FileAccessor abstraction that supports random-access writes at offsets.
    - Each segment writes into the correct offset in the same destination file.
    - Ensure flush/sync at safe intervals.
5) Pause:
    - Pausing stops all segment jobs, persists current offsets, transitions state to Paused.
6) Resume:
    - Loads persisted metadata, validates server identity (ETag or Last-Modified match if
      available).
    - Continues segments from last downloaded offsets.
7) Persistence:
    - Define a MetadataStore interface in common code:
      interface MetadataStore {
      suspend fun load(taskId: String): DownloadMetadata?
      suspend fun save(taskId: String, metadata: DownloadMetadata)
      suspend fun clear(taskId: String)
      }
    - Provide an in-memory default; also provide a file-based JSON implementation on JVM/Android (
      e.g., save to “.kdown/<taskId>.json” next to dest or in a configurable directory).
    - DownloadMetadata includes url, destPath, totalBytes, acceptRanges, etag, lastModified,
      segments progress list.
8) Error handling:
    - Create a sealed KDownError with types: Network, Http(code), Disk, Unsupported,
      ValidationFailed, Canceled, Unknown.
    - Retry only for transient network errors and 5xx.
9) Progress:
    - Aggregate across segments. Update progress StateFlow periodically (e.g., every 200ms) without
      spamming.
10) Tests:
    - Add unit tests for segment math, metadata serialization, state transitions.
    - Provide a simple fake HttpEngine to test resume logic.
11) Documentation:

- README with quickstart, features, limitations.
- Include a small JVM CLI sample demonstrating pause/resume.

## Implementation notes

- Favor simple correctness over micro-optimizations.
- Keep internal classes:
    - DownloadCoordinator (orchestrates jobs)
    - SegmentDownloader (downloads one segment)
    - RangeSupportDetector
    - FileAccessor (expect/actual)
    - JsonMetadataStore (JVM/Android)
- Avoid platform-specific APIs in commonMain except via expect/actual.
