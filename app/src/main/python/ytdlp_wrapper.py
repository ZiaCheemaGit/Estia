from ytmusicapi import YTMusic
from yt_dlp import YoutubeDL
import re

def yt_music_search(query: str):
    print("Starting......yt music search")
    ytmusic = YTMusic()
    results = ytmusic.search(query, filter="songs")
    return results

def get_official_youtube_video_id(song_name: str, artist_name: str) -> str:
    ytmusic = YTMusic()
    query = f'{song_name} {artist_name.replace(",", " ")}'
    results = ytmusic.search(query, filter="songs")

    artist_candidates = [a.strip().lower() for a in artist_name.split(",")]

    for result in results:
        result_artists = [a['name'].lower() for a in result['artists']]
        video_id = result.get('videoId')
        print(f"given song = {song_name.lower()} && returned song = {result.get('title').lower()}")
        print(f"given artists = {artist_candidates} && returned artists = {result_artists}")
        if any(candidate in result_artists for candidate in artist_candidates) and video_id:
                if song_name == re.sub(r'\s*\([^)]*\)', '', result["title"]):
                    return video_id

    return f2(song_name, artist_name)

def f2(song_name: str, artist_name: str) -> str | None:
    artist_candidates = [a.strip().lower() for a in artist_name.split(",")]
    query = f'{song_name} {artist_name}'
    ydl_opts = {
        "quiet": True,
        "skip_download": True,
        "extract_flat": "in_playlist",  # Only get metadata
        "default_search": "ytsearch5",  # Search top 5 results
    }

    with YoutubeDL(ydl_opts) as ydl:
        ytdlp_result = ydl.extract_info(query, download=False)

        if "entries" not in ytdlp_result:
            return None
        results = ytdlp_result["entries"]

        for result in results:
            uploader = result['uploader'].lower()
            video_id = result.get('videoId')
            print(f"given song = {song_name} && returned song = {result.get('title')}")
            print(f"given artists = {artist_candidates} && uploader = {uploader}")
            for artist_candidate in artist_candidates:
                if uploader == artist_candidate and video_id:
                        return video_id
                print(f"{artist_candidate} != {uploader}")


        # Fallback: return the first result
        return ytdlp_result["entries"][0].get("id") if ytdlp_result["entries"] else None