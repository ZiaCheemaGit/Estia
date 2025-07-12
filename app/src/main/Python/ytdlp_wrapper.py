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
                'title': info.get('title'),
                'url': info['url'],
                'ext': info['ext'],
                'duration': info.get('duration')
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

