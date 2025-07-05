from yt_dlp import YoutubeDL
import re
import os

##############
##############
##############

def get_song_info(query, cache_dir):
    print(f"[DEBUG] Searching for: {query}")
    cache_dir = str(cache_dir)
    fixed_filename = "cached_audio.m4a"
    full_path = os.path.join(cache_dir, fixed_filename)

    # Ensure cache directory exists
    os.makedirs(cache_dir, exist_ok=True)

    # Delete old file if it exists
    if os.path.exists(full_path):
        try:
            os.remove(full_path)
            print(f"[DEBUG] Deleted previous cached file: {full_path}")
        except Exception as e:
            print(f"[ERROR] Could not delete old file: {e}")

    search_opts = {
        'quiet': True,
        'format': 'bestaudio[ext=m4a]/bestaudio/best',
        'skip_download': True,
        'default_search': 'ytsearch1',
        'noplaylist': True,
        'user_agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
        'http_headers': {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
            'Accept-Language': 'en-US,en;q=0.9',
        },
    }

    try:
        with YoutubeDL(search_opts) as ydl:
            print("[DEBUG] Extracting info from yt-dlp...")
            info = ydl.extract_info(query, download=False)

            if 'entries' in info:
                info = info['entries'][0]
                print(f"[DEBUG] Search result found: {info.get('title')}")

            title = info.get("title") or "audio_track"
            ext = info.get("ext") or "m4a"
            webpage_url = info.get("webpage_url")

            print(f"[DEBUG] Downloading to: {full_path}")

            download_opts = {
                'format': 'bestaudio[ext=m4a]/bestaudio/best',
                'outtmpl': full_path,
                'quiet': True,
                'noplaylist': True,
                'http_headers': {
                    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
                    'Accept-Language': 'en-US,en;q=0.9',
                },
            }

            with YoutubeDL(download_opts) as ydl2:
                ydl2.download([webpage_url])
                print("[DEBUG] Download successful.")

            return {
                "title": title,
                "ext": ext,
                "path": full_path
            }

    except Exception as e:
        print(f"[ERROR] Exception: {e}")
        return {"error": str(e)}


##############
##############
##############


def get_song_audio_url(query):
    print(f"[DEBUG] Searching for: {query}")

    ydl_opts = {
        'format': 'bestaudio[ext=m4a]/bestaudio/best',
        'skip_download': True,
        'default_search': 'ytsearch1',
        'noplaylist': True,
        'quiet': True,
        'user_agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
        'http_headers': {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
            'Accept-Language': 'en-US,en;q=0.9',
        }
    }

    try:
        with YoutubeDL(ydl_opts) as ydl:
            print("[DEBUG] Extracting info from yt-dlp...")
            info = ydl.extract_info(query, download=False)

            # If it's a search result list, pick the first one
            if 'entries' in info:
                info = info['entries'][0]

            title = info.get('title', 'Unknown')
            audio_url = info.get('url')
            ext = info.get('ext', 'm4a')
            print(f"[DEBUG] Found audio URL: {audio_url}")

            return {
                'title': title,
                'url': audio_url,
                'ext': ext
            }

    except Exception as e:
        print(f"[ERROR] Exception occurred: {e}")
        return {'error': str(e)}


##############
##############
##############


def get_audio_url(video_url):
    ydl_opts = {
        'quiet': True,
        'format': 'bestaudio/best',
        'skip_download': True,
    }

    with YoutubeDL(ydl_opts) as ydl:
        try:
            info = ydl.extract_info(video_url, download=False)
            return {
                'title': info.get('title'),
                'url': info['url'],  # Direct audio stream URL
                'ext': info['ext'],  # File format (e.g. m4a, webm)
                'duration': info.get('duration')
            }
        except Exception as e:
            return {'error': str(e)}

