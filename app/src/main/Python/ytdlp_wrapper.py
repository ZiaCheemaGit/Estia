from yt_dlp import YoutubeDL
import re
import os
from ytmusicapi import YTMusic

##############
##############
##############


def get_audio_info_by_video_id(video_id: str):
    ydl_opts = {
        'quiet': True,
        'format': 'bestaudio/best',
        'skip_download': True,
    }

    with YoutubeDL(ydl_opts) as ydl:
        try:
            info = ydl.extract_info(video_id, download=False)
            return {
                'url': info.get('url'),
                'duration': info.get('duration'),
                'title': info.get('title'),
                'thumbnail': info.get('thumbnail'),
                'artist': info.get('artist'),
                'album': info.get('album', ''),
                'id': info.get('id'),
            }
        except Exception as e:
            return {'error': str(e)}



##############
##############
##############


def get_official_youtube_video_id(song_name: str, artist_name: str) -> str:
    ytmusic = YTMusic()
    query = f"{song_name} {artist_name}"
    results = ytmusic.search(query, filter="songs")

    artist_candidates = [a.strip().lower() for a in artist_name.split(",")]

    for result in results:
        result_artists = [a['name'].lower() for a in result['artists']]
        video_id = result.get('videoId')
        if any(candidate in result_artists for candidate in artist_candidates) and video_id:
            return video_id

    return results[0].get('videoId')


##############
##############
##############


def get_song_audio_url(query, artist):

    video_id = get_official_youtube_video_id(query, artist)
    print(f"[DEBUG] Found videoId: {video_id}")
    return get_audio_info_by_video_id(video_id)


##############
##############
##############


def get_playlist_song_info(playlist_url: str):
    ydl_opts = {
        'quiet': True,
        'extract_flat': True,  # Only get metadata, not full download
        'skip_download': True,
    }

    result = []

    with YoutubeDL(ydl_opts) as ydl:
        data = ydl.extract_info(playlist_url, download=False)
        playlist_title = data.get("title", "Unknown Playlist")

        if 'entries' in data:
            for entry in data['entries']:
                title = entry.get('title', 'Unknown Title')
                artist_guess = entry.get('artist') or entry.get('uploader') or ''
                print(f"yt music api artist = {artist_guess}, title = {title}")
                video = get_song_audio_url(title, artist_guess)
                result.append({
                    'playlist_title': playlist_title,
                    'title': video['title'],
                    'artists': video['artist'],
                    'url': video['url'],
                    'duration': video['duration'],
                    'thumbnail': video['thumbnail'],
                    'album': video['album'],
                    'id': video['id'],
                })

    print(f"[DEBUG] Returning list of Length: {len(result)}")
    return {
        'result':result
    }
