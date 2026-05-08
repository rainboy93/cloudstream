# Graph Report - app/src + library/src  (2026-05-08)

## Corpus Check

- 416 files · ~315,812 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary

- 3999 nodes · 3638 edges · 361 communities detected
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS
- Token cost: 0 input · 0 output

## God Nodes (most connected - your core abstractions)

1. `FullScreenPlayer` - 68 edges
2. `CS3IPlayer` - 54 edges
3. `GeneratorPlayer` - 52 edges
4. `ResultViewModel2` - 48 edges
5. `AppContextUtils` - 48 edges
6. `DataStoreHelper` - 47 edges
7. `IPlayer` - 33 edges
8. `BaseAdapter` - 32 edges
9. `UpdatedMatroskaExtractor` - 31 edges
10. `UIHelper` - 31 edges

## Surprising Connections (you probably didn't know these)

- None detected - all connections are within the same source files.

## Communities

### Community 0 - "AniList Sync API"

Cohesion: 0.02
Nodes (64): AniListApi, AniListAvatar, AniListData, AniListFavoritesMediaConnection,
AniListFavourites, AniListRoot, AniListStatusType, AniListTitleHolder (+56 more)

### Community 1 - "Simkl Tracking API"

Cohesion: 0.02
Nodes (32): Account, ActivitiesResponse, AllItemsResponse, Builder, CacheTimes, Episode,
EpisodeMetadata, HistoryMediaObject (+24 more)

### Community 2 - "Result ViewModel"

Cohesion: 0.03
Nodes (18): AutoResume, CheckDuplicateData, EpisodeIndexer, EpisodeRange, EpisodeSortType,
ExtractedTrailerData, ExtractorSubtitleLink, LibraryListType (+10 more)

### Community 3 - "MainAPI Provider System"

Cohesion: 0.03
Nodes (17): APIHolder, AudioFile, AutoDownloadMode, DubStatus, ErrorLoadingException,
HomePageResponse, MainAPI, MainPageData (+9 more)

### Community 4 - "Fullscreen Player UI"

Cohesion: 0.03
Nodes (2): FullScreenPlayer, TouchAction

### Community 5 - "ExoPlayer Integration"

Cohesion: 0.03
Nodes (3): CS3IPlayer, DrmMetadata, MediaItemSlice

### Community 6 - "Matroska Extractor"

Cohesion: 0.03
Nodes (6): CuePointData, Flags, InnerEbmlProcessor, MatroskaSeekMap, Track, UpdatedMatroskaExtractor

### Community 7 - "Player Interface"

Cohesion: 0.03
Nodes (28): AudioTrack, CSPlayerEvent, CSPlayerLoading, CurrentTracks, DownloadEvent,
EmbeddedSubtitlesFetchedEvent, EpisodeSeekEvent, ErrorEvent (+20 more)

### Community 8 - "DataStore Persistence"

Cohesion: 0.03
Nodes (9): Account, BookmarkedData, DataStoreHelper, FavoritesData, LibrarySearchResponse, PosDur,
ResumeWatchingResult, SubscribedData (+1 more)

### Community 9 - "HLS Playlist Parser"

Cohesion: 0.03
Nodes (16): C, DrmInitData, Format, HlsMultivariantPlaylist, HlsPlaylistParser, MimeTypes,
Mp4aObjectType, Mp4Box (+8 more)

### Community 10 - "Generator Player Logic"

Cohesion: 0.04
Nodes (2): GeneratorPlayer, TempMetaData

### Community 11 - "MAL Sync API"

Cohesion: 0.04
Nodes (31): AlternativeTitles, Broadcast, Data, Genres, ListStatus, MainPicture, MalAnime, MALApi (
+23 more)

### Community 12 - "Main Activity"

Cohesion: 0.04
Nodes (4): FocusTarget, MainActivity, SessionManagerListenerImpl, TvFocus

### Community 13 - "App Context Utilities"

Cohesion: 0.04
Nodes (1): AppContextUtils

### Community 14 - "Base RecyclerView Adapter"

Cohesion: 0.04
Nodes (4): BaseAdapter, BaseDiffCallback, NoStateAdapter, ViewHolderState

### Community 15 - "Download Manager"

Cohesion: 0.04
Nodes (6): DownloadActionType, DownloadMetaData, DownloadType, EpisodeDownloadInstance,
LazyStreamDownloadData, VideoDownloadManager

### Community 16 - "Kitsu Sync API"

Cohesion: 0.04
Nodes (23): Data, Description, Episodes, Kitsu, KitsuAnimeAttributes, KitsuAnimeData, KitsuApi,
KitsuLinks (+15 more)

### Community 17 - "MyDramaList Provider"

Cohesion: 0.05
Nodes (22): Cast, Credits, Crew, Data, Genre, Images, LinkData, Media (+14 more)

### Community 18 - "UI Helper Utilities"

Cohesion: 0.05
Nodes (2): CutoutOverlayDrawable, UIHelper

### Community 19 - "StreamSB Extractor"

Cohesion: 0.06
Nodes (32): Keephealth, Lvturbo, Main, Sbasian, Sbface, Sbflix, SBfull, Sblona (+24 more)

### Community 20 - "Preview Generator"

Cohesion: 0.06
Nodes (6): ImageParams, IPreviewGenerator, M3u8PreviewGenerator, Mp4PreviewGenerator,
NoPreviewGenerator, PreviewGenerator

### Community 21 - "Download ViewModel"

Cohesion: 0.06
Nodes (3): DeleteData, DownloadStats, DownloadViewModel

### Community 22 - "Abstract Player Fragment"

Cohesion: 0.06
Nodes (2): AbstractPlayerFragment, PlayerResize

### Community 23 - "Plugin Manager"

Cohesion: 0.06
Nodes (3): OnlinePluginData, PluginData, PluginManager

### Community 24 - "StreamWish Extractor"

Cohesion: 0.06
Nodes (28): Asnwish, Awish, CdnwishCom, Doodporn, Dwish, Ewish, FlaswishCom, HlsWish (+20 more)

### Community 25 - "Base Fragment System"

Cohesion: 0.07
Nodes (8): BaseBottomSheetDialogFragment, BaseDialogFragment, BaseFragment, BaseFragmentHelper,
BasePreferenceFragmentCompat, Bind, BindingCreator, Inflate

### Community 26 - "Default Extractors Factory"

Cohesion: 0.07
Nodes (3): ConstructorSupplier, ExtensionLoader, UpdatedDefaultExtractorsFactory

### Community 27 - "Home ViewModel"

Cohesion: 0.08
Nodes (2): ExpandableHomepageList, HomeViewModel

### Community 28 - "Download Objects"

Cohesion: 0.08
Nodes (16): CreateNotificationMetadata, DownloadCached, DownloadedFileInfo,
DownloadedFileInfoResult, DownloadEpisodeCached, DownloadEpisodeMetadata, DownloadHeaderCached,
DownloadItem (+8 more)

### Community 29 - "Extractor API Base"

Cohesion: 0.08
Nodes (7): DrmExtractorLink, ExtractorApi, ExtractorLink, ExtractorLinkPlayList, ExtractorLinkType,
PlayListItem, Qualities

### Community 30 - "Sync ViewModel"

Cohesion: 0.08
Nodes (2): CurrentSynced, SyncViewModel

### Community 31 - "Subtitles Fragment"

Cohesion: 0.08
Nodes (2): SaveCaptionStyle, SubtitlesFragment

### Community 32 - "Auth API"

Cohesion: 0.08
Nodes (9): AuthAPI, AuthData, AuthLoginPage, AuthLoginRequirement, AuthLoginResponse, AuthPinData,
AuthToken, AuthUser (+1 more)

### Community 33 - "Dood Extractor"

Cohesion: 0.08
Nodes (20): D0000d, D000dCom, DoodCxExtractor, DoodLaExtractor, DoodLiExtractor, DoodPmExtractor,
DoodShExtractor, DoodSoExtractor (+12 more)

### Community 34 - "Result Fragment Phone"

Cohesion: 0.09
Nodes (1): ResultFragmentPhone

### Community 35 - "Subtitle Helper"

Cohesion: 0.09
Nodes (3): Language639, LanguageMetadata, SubtitleHelper

### Community 36 - "Link Generator Interface"

Cohesion: 0.1
Nodes (3): IGenerator, NoVideoGenerator, VideoGenerator

### Community 37 - "OPhim Provider"

Cohesion: 0.1
Nodes (12): ListDataResponse, ListResponse, MappedData, MappedEpisode, MappedEpisodeItem,
MovieDetailResponse, MovieEpisodeDataResponse, MovieEpisodeResponse (+4 more)

### Community 38 - "DataStore Core"

Cohesion: 0.1
Nodes (3): DataStore, Editor, PreferenceDelegate

### Community 39 - "Trakt Provider"

Cohesion: 0.1
Nodes (12): Airs, Cast, Data, Ids, Images, LinkData, MediaDetails, People (+4 more)

### Community 40 - "Arch Component Extensions"

Cohesion: 0.1
Nodes (5): DebugException, Failure, Loading, Resource, Success

### Community 41 - "Common Activity Base"

Cohesion: 0.1
Nodes (2): CommonActivity, FocusDirection

### Community 42 - "Home Preview Adapter"

Cohesion: 0.1
Nodes (2): HeaderViewHolder, HomeParentItemAdapterPreview

### Community 43 - "M3U8 Download Helper"

Cohesion: 0.1
Nodes (5): LazyHlsDownloadData, M3u8Helper, M3u8Helper2, M3u8Stream, TsLink

### Community 44 - "XStreamCDN Extractor"

Cohesion: 0.1
Nodes (17): Captions, Cdnplayer, DBfilm, FeHD, FEmbed, Fembed9hd, FEnet, Fplayer (+9 more)

### Community 45 - "Byses Encryption"

Cohesion: 0.1
Nodes (11): ByseBuho, ByseQekaho, ByseSX, ByseVepoin, Bysezejataos, DecryptKeys, DetailsRoot,
Playback (+3 more)

### Community 46 - "Result Trailer Player"

Cohesion: 0.11
Nodes (1): ResultTrailerPlayer

### Community 47 - "Download Adapter"

Cohesion: 0.11
Nodes (7): Child, DiffCallback, DownloadAdapter, DownloadClickEvent, DownloadHeaderClickEvent,
Header, VisualDownloadCached

### Community 48 - "Player Generator ViewModel"

Cohesion: 0.11
Nodes (1): PlayerGeneratorViewModel

### Community 49 - "Testing Utilities"

Cohesion: 0.11
Nodes (8): Logger, LogLevel, Message, TestingUtils, TestResult, TestResultList, TestResultLoad,
TestResultProvider

### Community 50 - "Result Fragment TV"

Cohesion: 0.11
Nodes (1): ResultFragmentTv

### Community 51 - "Custom Subtitle Decoder"

Cohesion: 0.11
Nodes (3): CustomDecoder, CustomSubtitleDecoderFactory, DelegatingSubtitleDecoder

### Community 52 - "Sync API Interface"

Cohesion: 0.11
Nodes (10): AbstractSyncStatus, LibraryItem, LibraryList, LibraryMetadata, Page, SyncAPI,
SyncIdName, SyncResult (+2 more)

### Community 53 - "FCast Manager"

Cohesion: 0.11
Nodes (4): DefaultDiscoveryListener, DefaultRegistrationListener, FcastManager, PublicDeviceInfo

### Community 54 - "GDrive Player Extractor"

Cohesion: 0.11
Nodes (13): DatabaseGdrive, DatabaseGdrive2, Gdriveplayer, Gdriveplayerapi, Gdriveplayerapp,
Gdriveplayerbiz, Gdriveplayerco, Gdriveplayerfun (+5 more)

### Community 55 - "Home Fragment UI"

Cohesion: 0.12
Nodes (1): HomeFragment

### Community 56 - "Chromecast Subtitles"

Cohesion: 0.12
Nodes (2): ChromecastSubtitlesFragment, SaveChromeCaptionStyle

### Community 57 - "Player Subtitle Helper"

Cohesion: 0.12
Nodes (4): PlayerSubtitleHelper, SubtitleData, SubtitleOrigin, SubtitleStatus

### Community 58 - "Torrent Support"

Cohesion: 0.12
Nodes (4): Torrent, TorrentFileStat, TorrentRequest, TorrentStatus

### Community 59 - "Quality Data Helper"

Cohesion: 0.12
Nodes (3): QualityDataHelper, QualityProfile, QualityProfileType

### Community 60 - "OpenSubtitles API"

Cohesion: 0.12
Nodes (8): OAuthToken, OpenSubtitlesApi, ResultAttributes, ResultData, ResultDownloadLink,
ResultFeatureDetails, ResultFiles, Results

### Community 61 - "Download Queue Manager"

Cohesion: 0.12
Nodes (1): DownloadQueueManager

### Community 62 - "RabbitStream Extractor"

Cohesion: 0.12
Nodes (7): Dokicloud, Megacloud, Rabbitstream, Sources, SourcesEncrypted, SourcesResponses, Tracks

### Community 63 - "CloudStream App Class"

Cohesion: 0.12
Nodes (2): CloudStreamApp, ExceptionHandler

### Community 64 - "Fetch Button Widget"

Cohesion: 0.12
Nodes (2): BaseFetchButton, DownloadMetadata

### Community 65 - "In-App Updater"

Cohesion: 0.12
Nodes (6): GithubAsset, GithubObject, GithubRelease, GithubTag, InAppUpdater, Update

### Community 66 - "Video Click Action"

Cohesion: 0.12
Nodes (2): VideoClickAction, VideoClickActionHolder

### Community 67 - "Voe Extractor"

Cohesion: 0.12
Nodes (8): MetaGnathTuggers, NathanFromSubject, Simpulumlamerop, Tubeless, Urochsunloath, Voe, Voe1,
Yipsu

### Community 68 - "Plugins ViewModel"

Cohesion: 0.13
Nodes (1): PluginsViewModel

### Community 69 - "Home Child Adapter"

Cohesion: 0.13
Nodes (3): HomeChildItemAdapter, HomeScrollViewHolderState, ResumeItemAdapter

### Community 70 - "Download Queue Adapter"

Cohesion: 0.13
Nodes (4): DownloadAdapterItem, DownloadQueueAdapter, DragAndDropTouchHelper,
DragAndDropTouchHelperCallback

### Community 71 - "Text Utilities"

Cohesion: 0.13
Nodes (3): DynamicString, StringResource, UiText

### Community 72 - "Backup Utilities"

Cohesion: 0.13
Nodes (3): BackupFile, BackupUtils, BackupVars

### Community 73 - "TMDB Provider"

Cohesion: 0.13
Nodes (2): TmdbLink, TmdbProvider

### Community 74 - "Cinemm Redirect Extractor"

Cohesion: 0.13
Nodes (13): Auvexiug, Cavanhabg, CineMMRedirect, Dhcplay, Dumbalag, Guxhag, Habetar, Haxloppd (+5
more)

### Community 75 - "VidHide Extractor"

Cohesion: 0.13
Nodes (12): Dhtpre, Peytonepre, Ryderjet, Smoothpre, VidHideHub, VidHidePro, VidHidePro1,
VidHidePro2 (+4 more)

### Community 76 - "Controller Activity"

Cohesion: 0.14
Nodes (5): ControllerActivity, MetadataHolder, SelectSourceController, SkipNextEpisodeController,
SkipTimeController

### Community 77 - "Library Fragment"

Cohesion: 0.14
Nodes (5): LibraryFragment, LibraryOpener, LibraryOpenerType, MenuSearchView, ProviderLibraryData

### Community 78 - "Repository Manager"

Cohesion: 0.14
Nodes (3): Repository, RepositoryManager, SitePlugin

### Community 79 - "Auth Repository"

Cohesion: 0.14
Nodes (1): AuthRepo

### Community 80 - "Subdl"

Cohesion: 0.14
Nodes (9): ApiKeyResponse, ApiResponse, OAuthTokenResponse, Result, SubDlApi, Subtitle,
SubtitleOAuthEntity, Usage (+1 more)

### Community 81 - "Apirepository"

Cohesion: 0.15
Nodes (2): APIRepository, SavedLoadResponse

### Community 82 - "Resultfragment"

Cohesion: 0.15
Nodes (4): ResultEpisode, ResultFragment, StoredData, VideoWatchState

### Community 83 - "Searchfragment"

Cohesion: 0.15
Nodes (1): SearchFragment

### Community 84 - "Downloadfragment"

Cohesion: 0.15
Nodes (1): DownloadFragment

### Community 85 - "Unshortenurl"

Cohesion: 0.15
Nodes (2): ShortLink, ShortUrl

### Community 86 - "Settingsaccount"

Cohesion: 0.17
Nodes (1): SettingsAccount

### Community 87 - "Settingsfragment"

Cohesion: 0.17
Nodes (1): SettingsFragment

### Community 88 - "Linearlistlayout"

Cohesion: 0.17
Nodes (1): LinearListLayout

### Community 89 - "Searchviewmodel"

Cohesion: 0.17
Nodes (2): ExpandableSearchList, SearchViewModel

### Community 90 - "Subsource"

Cohesion: 0.17
Nodes (9): ApiResponse, ApiSearch, Found, Movie, Sub, SubData, SubSourceApi, SubTitleLink (+1 more)

### Community 91 - "Mixdrop"

Cohesion: 0.17
Nodes (9): Mdy, MixDrop, MixDropAg, MixDropBz, MixDropCh, MixDropPs, MixDropSi, MixDropTo (+1 more)

### Community 92 - "Exampleinstrumentedtest"

Cohesion: 0.18
Nodes (2): ExampleInstrumentedTest, TestApplication

### Community 93 - "Subtitleselectiontest"

Cohesion: 0.18
Nodes (1): SubtitleLanguageTagTest

### Community 94 - "Customrecyclerviews"

Cohesion: 0.18
Nodes (3): AutofitRecyclerView, GrdLayoutManager, MaxRecyclerView

### Community 95 - "Testviewmodel"

Cohesion: 0.18
Nodes (3): ProviderFilter, TestProgress, TestViewModel

### Community 96 - "Homeparentitemadapter"

Cohesion: 0.18
Nodes (3): LoadClickCallback, ParentItemAdapter, ParentItemHolder

### Community 97 - "Syncutil"

Cohesion: 0.18
Nodes (7): Anilist, Mal, MalSyncPage, ProviderPage, SyncPage, SyncPages, SyncUtil

### Community 98 - "Singleselectionhelper"

Cohesion: 0.18
Nodes (1): SingleSelectionHelper

### Community 99 - "Biometricauthenticator"

Cohesion: 0.18
Nodes (2): BiometricAuthenticator, BiometricCallback

### Community 100 - "Aniskip"

Cohesion: 0.18
Nodes (7): AniSkip, AniSkipInterval, AniSkipResponse, EpisodeSkip, SkipStamp, SkipType, Stamp

### Community 101 - "Dailymotion"

Cohesion: 0.18
Nodes (6): Dailymotion, Geodailymotion, MetaData, Quality, SubtitleData, SubtitlesWrapper

### Community 102 - "Filesim"

Cohesion: 0.18
Nodes (9): Ahvsh, Filesim, Guccihide, Movhide, Moviesm4u, Multimoviesshg, StreamhideCom,
StreamhideTo (+1 more)

### Community 103 - "Eastereggmonkefragment"

Cohesion: 0.2
Nodes (1): EasterEggMonkeFragment

### Community 104 - "Testview"

Cohesion: 0.2
Nodes (2): TestState, TestView

### Community 105 - "Piefetchbutton"

Cohesion: 0.2
Nodes (1): PieFetchButton

### Community 106 - "Votingapi"

Cohesion: 0.2
Nodes (2): CountifyResult, VotingApi

### Community 107 - "Cloudstreampackage"

Cohesion: 0.2
Nodes (3): CloudStreamPackage, MinimalSubtitleLink, MinimalVideoLink

### Community 108 - "Packets"

Cohesion: 0.2
Nodes (9): Opcode, PlaybackErrorMessage, PlaybackUpdateMessage, PlayMessage, SeekMessage,
SetSpeedMessage, SetVolumeMessage, VersionMessage (+1 more)

### Community 109 - "Packageinstallerservice"

Cohesion: 0.2
Nodes (1): PackageInstallerService

### Community 110 - "Gofile"

Cohesion: 0.2
Nodes (6): AccountData, AccountResponse, Gofile, GofileData, GofileFile, GofileResponse

### Community 111 - "Accountadapter"

Cohesion: 0.22
Nodes (2): AccountAdapter, AccountClickCallback

### Community 112 - "Pluginadapter"

Cohesion: 0.22
Nodes (3): PluginAdapter, PluginViewData, RepositoryViewHolderState

### Community 113 - "Searchhistoryadaptor"

Cohesion: 0.22
Nodes (3): SearchHistoryAdaptor, SearchHistoryCallback, SearchHistoryItem

### Community 114 - "Playerpiphelper"

Cohesion: 0.22
Nodes (1): PlayerPipHelper

### Community 115 - "Customsubripparser"

Cohesion: 0.22
Nodes (1): CustomSubripParser

### Community 116 - "Dohproviders"

Cohesion: 0.22
Nodes (0):

### Community 117 - "Cloudflarekiller"

Cohesion: 0.22
Nodes (1): CloudflareKiller

### Community 118 - "Packageinstaller"

Cohesion: 0.22
Nodes (3): ApkInstaller, DelayedInstaller, InstallProgressStatus

### Community 119 - "Tvchannelutils"

Cohesion: 0.22
Nodes (1): TvChannelUtils

### Community 120 - "Event"

Cohesion: 0.22
Nodes (2): EmptyEvent, Event

### Community 121 - "Downloadfilemanagement"

Cohesion: 0.22
Nodes (1): DownloadFileManagement

### Community 122 - "Openinappaction"

Cohesion: 0.22
Nodes (1): OpenInAppAction

### Community 123 - "Abstractsubprovider"

Cohesion: 0.22
Nodes (2): SingleSubtitleResource, SubtitleResource

### Community 124 - "Downloadqueueservice"

Cohesion: 0.22
Nodes (1): DownloadQueueService

### Community 125 - "Vidmoly"

Cohesion: 0.22
Nodes (6): Source, SubSource, Vidmoly, Vidmolybiz, Vidmolyme, Vidmolyto

### Community 126 - "Youtubeextractor"

Cohesion: 0.22
Nodes (4): YoutubeExtractor, YoutubeMobileExtractor, YoutubeNoCookieExtractor,
YoutubeShortLinkExtractor

### Community 127 - "Cda"

Cohesion: 0.22
Nodes (3): Cda, PlayerData, VideoPlayerData

### Community 128 - "Hxfile"

Cohesion: 0.22
Nodes (7): Aico, Hxfile, KotakAnimeid, Neonime7n, Neonime8n, ResponseSource, Yufiles

### Community 129 - "Jwplayer"

Cohesion: 0.22
Nodes (7): DesuArcg, DesuDrive, DesuOdchan, DesuOdvip, JWPlayer, Meownime, ResponseSource

### Community 130 - "Nineanimehelper"

Cohesion: 0.22
Nodes (1): NineAnimeHelper

### Community 131 - "Globals"

Cohesion: 0.25
Nodes (1): Globals

### Community 132 - "Extensionsfragment"

Cohesion: 0.25
Nodes (1): ExtensionsFragment

### Community 133 - "Episodeadapter"

Cohesion: 0.25
Nodes (2): EpisodeAdapter, EpisodeClickEvent

### Community 134 - "Setupfragmentextensions"

Cohesion: 0.25
Nodes (1): SetupFragmentExtensions

### Community 135 - "Libraryviewmodel"

Cohesion: 0.25
Nodes (2): LibraryViewModel, ListSorting

### Community 136 - "Viewpageradapter"

Cohesion: 0.25
Nodes (2): ViewpagerAdapter, ViewpagerAdapterViewHolderState

### Community 137 - "Quicksearchfragment"

Cohesion: 0.25
Nodes (1): QuickSearchFragment

### Community 138 - "Searchsuggestionadapter"

Cohesion: 0.25
Nodes (2): SearchSuggestionAdapter, SearchSuggestionCallback

### Community 139 - "Accountviewmodel"

Cohesion: 0.25
Nodes (1): AccountViewModel

### Community 140 - "Linkgenerator"

Cohesion: 0.25
Nodes (4): BasicLink, ExtractorUri, LinkGenerator, MinimalLinkGenerator

### Community 141 - "Flowlayout"

Cohesion: 0.25
Nodes (2): FlowLayout, LayoutParams

### Community 142 - "Centerzoomlayoutmanager"

Cohesion: 0.25
Nodes (1): CenterZoomLayoutManager

### Community 143 - "Accountmanager"

Cohesion: 0.25
Nodes (1): AccountManager

### Community 144 - "Backpressedcallbackhelper"

Cohesion: 0.25
Nodes (2): BackPressedCallbackHelper, CallbackHelper

### Community 145 - "Percentagecropimageview"

Cohesion: 0.25
Nodes (1): PercentageCropImageView

### Community 146 - "Downloadutils"

Cohesion: 0.25
Nodes (1): DownloadUtils

### Community 147 - "Peacemakerstextractor"

Cohesion: 0.25
Nodes (6): PeaceMakerst, PeaceResponse, Teve2ApiResponse, Teve2Link, Teve2Media, VideoSource

### Community 148 - "Vidstack"

Cohesion: 0.25
Nodes (3): AesHelper, Server1uns, VidStack

### Community 149 - "Gogohelper"

Cohesion: 0.25
Nodes (4): GogoHelper, GogoJsonData, GogoSource, GogoSources

### Community 150 - "Wcohelper"

Cohesion: 0.25
Nodes (3): ExternalKeys, NewExternalKeys, WcoHelper

### Community 151 - "Webviewfragment"

Cohesion: 0.29
Nodes (2): RepoApi, WebviewFragment

### Community 152 - "Settingsgeneral"

Cohesion: 0.29
Nodes (2): CustomSite, SettingsGeneral

### Community 153 - "Extensionsviewmodel"

Cohesion: 0.29
Nodes (3): ExtensionsViewModel, PluginStats, RepositoryData

### Community 154 - "Downloadchildfragment"

Cohesion: 0.29
Nodes (1): DownloadChildFragment

### Community 155 - "Downloadedplayeractivity"

Cohesion: 0.29
Nodes (1): DownloadedPlayerActivity

### Community 156 - "Subtitleoffsetitemadapter"

Cohesion: 0.29
Nodes (2): SubtitleCue, SubtitleOffsetItemAdapter

### Community 157 - "Fillerepisodecheck"

Cohesion: 0.29
Nodes (1): FillerEpisodeCheck

### Community 158 - "Imageutil"

Cohesion: 0.29
Nodes (4): Bitmap, Drawable, Image, UiImage

### Community 159 - "Powermanagerapi"

Cohesion: 0.29
Nodes (1): BatteryOptimizationChecker

### Community 160 - "Vector2"

Cohesion: 0.29
Nodes (1): Vector2

### Community 161 - "Fcastsession"

Cohesion: 0.29
Nodes (1): FcastSession

### Community 162 - "Parcollections"

Cohesion: 0.29
Nodes (0):

### Community 163 - "Baseplugin"

Cohesion: 0.29
Nodes (2): BasePlugin, Manifest

### Community 164 - "Jsunpacker"

Cohesion: 0.29
Nodes (2): JsUnpacker, Unbase

### Community 165 - "Crosstmdbprovider"

Cohesion: 0.29
Nodes (2): CrossMetaData, CrossTmdbProvider

### Community 166 - "Uqload"

Cohesion: 0.29
Nodes (5): Uqload, Uqload1, Uqload2, Uqloadbz, Uqloadcx

### Community 167 - "Videa"

Cohesion: 0.29
Nodes (1): Videa

### Community 168 - "Streamtape"

Cohesion: 0.29
Nodes (5): ShaveTape, StreamTape, StreamTapeNet, StreamTapeXyz, Watchadsontape

### Community 169 - "Linkbox"

Cohesion: 0.29
Nodes (5): Data, ItemInfo, Linkbox, Resolutions, Responses

### Community 170 - "Hotlingerextractor"

Cohesion: 0.29
Nodes (6): FourCX, FourPichive, FourPlayRu, Hotlinger, Pichive, PlayRu

### Community 171 - "Webviewresolver"

Cohesion: 0.29
Nodes (1): WebViewResolver

### Community 172 - "Nonfinaladapterlistupdatecallback"

Cohesion: 0.33
Nodes (1): NonFinalAdapterListUpdateCallback

### Community 173 - "Plugindetailsfragment"

Cohesion: 0.33
Nodes (1): PluginDetailsFragment

### Community 174 - "Pluginsfragment"

Cohesion: 0.33
Nodes (1): PluginsFragment

### Community 175 - "Testresultadapter"

Cohesion: 0.33
Nodes (1): TestResultAdapter

### Community 176 - "Selectadaptor"

Cohesion: 0.33
Nodes (1): SelectAdaptor

### Community 177 - "Loadingposteradapter"

Cohesion: 0.33
Nodes (1): LoadingPosterAdapter

### Community 178 - "Searchadaptor"

Cohesion: 0.33
Nodes (2): SearchAdapter, SearchClickCallback

### Community 179 - "Searchsuggestionapi"

Cohesion: 0.33
Nodes (3): SearchSuggestionApi, TmdbSearchItem, TmdbSearchResult

### Community 180 - "Downloadbutton"

Cohesion: 0.33
Nodes (1): DownloadButton

### Community 181 - "Accountselectactivity"

Cohesion: 0.33
Nodes (1): AccountSelectActivity

### Community 182 - "Profilesadapter"

Cohesion: 0.33
Nodes (1): ProfilesAdapter

### Community 183 - "Subtitleapi"

Cohesion: 0.33
Nodes (1): SubtitleAPI

### Community 184 - "Subtitlerepo"

Cohesion: 0.33
Nodes (3): SavedResourceResponse, SavedSearchResponse, SubtitleRepo

### Community 185 - "Syncrepo"

Cohesion: 0.33
Nodes (1): SyncRepo

### Community 186 - "Consistentlivedata"

Cohesion: 0.33
Nodes (2): ConsistentLiveData, ResourceLiveData

### Community 187 - "Imagemodulecoil"

Cohesion: 0.33
Nodes (1): ImageLoader

### Community 188 - "Mpvpackage"

Cohesion: 0.33
Nodes (3): MpvExPackage, MpvPackage, MpvYTDLPackage

### Community 189 - "Log"

Cohesion: 0.33
Nodes (1): Log

### Community 190 - "Jshunter"

Cohesion: 0.33
Nodes (1): JsHunter

### Community 191 - "Videoseyredextractor"

Cohesion: 0.33
Nodes (4): VideoSeyred, VideoSeyredSource, VSSource, VSTrack

### Community 192 - "Userload"

Cohesion: 0.33
Nodes (1): Userload

### Community 193 - "Vidoza"

Cohesion: 0.33
Nodes (4): Videzz, Vidoza, VinovoDataList, VinovoVideoData

### Community 194 - "Lulustream"

Cohesion: 0.33
Nodes (4): LuluStream, Lulustream1, Lulustream2, Luluvdoo

### Community 195 - "Zplayer"

Cohesion: 0.33
Nodes (4): Streamhub2, Upstream, Zplayer, ZplayerV2

### Community 196 - "Streamlare"

Cohesion: 0.33
Nodes (4): JsonResponse, Result, Slmaxed, Streamlare

### Community 197 - "Secvideoonline"

Cohesion: 0.33
Nodes (4): CsstOnline, DsstOnline, FsstOnline, SecvideoOnline

### Community 198 - "Filemoon"

Cohesion: 0.33
Nodes (4): FileMoon, FileMoonIn, FileMoonSx, FilemoonV2

### Community 199 - "Jeniusplay"

Cohesion: 0.33
Nodes (3): Jeniusplay, ResponseSource, Tracks

### Community 200 - "Hubcloud"

Cohesion: 0.33
Nodes (1): HubCloud

### Community 201 - "Cryptojshelper"

Cohesion: 0.33
Nodes (1): CryptoJS

### Community 202 - "Aeshelper"

Cohesion: 0.33
Nodes (2): AesData, AesHelper

### Community 203 - "Downloadertestimpl"

Cohesion: 0.4
Nodes (1): DownloaderTestImpl

### Community 204 - "Acraapplication"

Cohesion: 0.4
Nodes (1): AcraApplication

### Community 205 - "Minicontrollerfragment"

Cohesion: 0.4
Nodes (1): MyMiniControllerFragment

### Community 206 - "Settingsupdates"

Cohesion: 0.4
Nodes (1): SettingsUpdates

### Community 207 - "Repoadapter"

Cohesion: 0.4
Nodes (1): RepoAdapter

### Community 208 - "Homescrolladapter"

Cohesion: 0.4
Nodes (1): HomeScrollAdapter

### Community 209 - "Imageadapter"

Cohesion: 0.4
Nodes (1): ImageAdapter

### Community 210 - "Actoradaptor"

Cohesion: 0.4
Nodes (1): ActorAdaptor

### Community 211 - "Pageadapter"

Cohesion: 0.4
Nodes (1): PageAdapter

### Community 212 - "Accounthelper"

Cohesion: 0.4
Nodes (1): AccountHelper

### Community 213 - "Ssltrustmanager"

Cohesion: 0.4
Nodes (1): SSLTrustManager

### Community 214 - "Offlineplaybackhelper"

Cohesion: 0.4
Nodes (1): OfflinePlaybackHelper

### Community 215 - "Priorityadapter"

Cohesion: 0.4
Nodes (2): PriorityAdapter, SourcePriority

### Community 216 - "Qualityprofiledialog"

Cohesion: 0.4
Nodes (2): LinkSource, QualityProfileDialog

### Community 217 - "Addic7Ed"

Cohesion: 0.4
Nodes (1): Addic7ed

### Community 218 - "Subtitleutils"

Cohesion: 0.4
Nodes (1): SubtitleUtils

### Community 219 - "Idisposable"

Cohesion: 0.4
Nodes (2): IDisposable, IDisposableHelper

### Community 220 - "Casthelper"

Cohesion: 0.4
Nodes (1): CastHelper

### Community 221 - "Mpvktpackage"

Cohesion: 0.4
Nodes (2): MpvKtPackage, MpvKtPreviewPackage

### Community 222 - "Vlcpackage"

Cohesion: 0.4
Nodes (2): VlcNightlyPackage, VlcPackage

### Community 223 - "Fcastaction"

Cohesion: 0.4
Nodes (1): FcastAction

### Community 224 - "Subscriptionworkmanager"

Cohesion: 0.4
Nodes (1): SubscriptionWorkManager

### Community 225 - "Videodownloadservice"

Cohesion: 0.4
Nodes (1): VideoDownloadService

### Community 226 - "Coroutines"

Cohesion: 0.4
Nodes (0):

### Community 227 - "Apputils"

Cohesion: 0.4
Nodes (1): AppUtils

### Community 228 - "Vinovo"

Cohesion: 0.4
Nodes (3): VinovoFileResp, VinovoSi, VinovoTo

### Community 229 - "Pelisplus"

Cohesion: 0.4
Nodes (1): Pelisplus

### Community 230 - "Vicloud"

Cohesion: 0.4
Nodes (3): Responses, Sources, Vicloud

### Community 231 - "Gdmirrorbot"

Cohesion: 0.4
Nodes (2): GDMirrorbot, Techinmind

### Community 232 - "Streamup"

Cohesion: 0.4
Nodes (3): Streamix, Streamup, StreamUpFileInfo

### Community 233 - "Vidstream"

Cohesion: 0.4
Nodes (1): Vidstream

### Community 234 - "Bigwarp"

Cohesion: 0.4
Nodes (3): BgwpCC, BigwarpArt, BigwarpIO

### Community 235 - "Tauvideoextractor"

Cohesion: 0.4
Nodes (3): TauVideo, TauVideoData, TauVideoUrls

### Community 236 - "Okruextractor"

Cohesion: 0.4
Nodes (4): OkRuHTTP, OkRuHTTPMobile, OkRuSSL, OkRuSSLMobile

### Community 237 - "Mailruextractor"

Cohesion: 0.4
Nodes (3): MailRu, MailRuData, MailRuVideoData

### Community 238 - "Tantifilm"

Cohesion: 0.4
Nodes (3): Tantifilm, TantifilmData, TantifilmJsonData

### Community 239 - "Settingsplayer"

Cohesion: 0.5
Nodes (1): SettingsPlayer

### Community 240 - "Settingsui"

Cohesion: 0.5
Nodes (1): SettingsUI

### Community 241 - "Logcatadapter"

Cohesion: 0.5
Nodes (1): LogcatAdapter

### Community 242 - "Settingsproviders"

Cohesion: 0.5
Nodes (1): SettingsProviders

### Community 243 - "Testfragment"

Cohesion: 0.5
Nodes (1): TestFragment

### Community 244 - "Setupfragmentlayout"

Cohesion: 0.5
Nodes (1): SetupFragmentLayout

### Community 245 - "Setupfragmentlanguage"

Cohesion: 0.5
Nodes (1): SetupFragmentLanguage

### Community 246 - "Setupfragmentproviderlanguage"

Cohesion: 0.5
Nodes (1): SetupFragmentProviderLanguage

### Community 247 - "Setupfragmentmedia"

Cohesion: 0.5
Nodes (1): SetupFragmentMedia

### Community 248 - "Searchresultbuilder"

Cohesion: 0.5
Nodes (1): SearchResultBuilder

### Community 249 - "Downloadqueuefragment"

Cohesion: 0.5
Nodes (1): DownloadQueueFragment

### Community 250 - "Downloadqueueviewmodel"

Cohesion: 0.5
Nodes (2): DownloadAdapterQueue, DownloadQueueViewModel

### Community 251 - "Repolinkgenerator"

Cohesion: 0.5
Nodes (2): Cache, RepoLinkGenerator

### Community 252 - "Plugin"

Cohesion: 0.5
Nodes (1): Plugin

### Community 253 - "Requestshelper"

Cohesion: 0.5
Nodes (0):

### Community 254 - "Ddosguardkiller"

Cohesion: 0.5
Nodes (1): DdosGuardKiller

### Community 255 - "Backupapi"

Cohesion: 0.5
Nodes (1): BackupAPI

### Community 256 - "Castoptionsprovider"

Cohesion: 0.5
Nodes (1): CastOptionsProvider

### Community 257 - "Alwaysaskaction"

Cohesion: 0.5
Nodes (1): AlwaysAskAction

### Community 258 - "Playinbrowseraction"

Cohesion: 0.5
Nodes (1): PlayInBrowserAction

### Community 259 - "Playmirroraction"

Cohesion: 0.5
Nodes (1): PlayMirrorAction

### Community 260 - "Libretorrentpackage"

Cohesion: 0.5
Nodes (1): LibreTorrentPackage

### Community 261 - "Webvideocastpackage"

Cohesion: 0.5
Nodes (1): WebVideoCastPackage

### Community 262 - "Nextplayerpackage"

Cohesion: 0.5
Nodes (1): NextPlayerPackage

### Community 263 - "Justplayerpackage"

Cohesion: 0.5
Nodes (1): JustPlayerPackage

### Community 264 - "Biglybtpackage"

Cohesion: 0.5
Nodes (1): BiglyBTPackage

### Community 265 - "Aria2Package"

Cohesion: 0.5
Nodes (1): Aria2Package

### Community 266 - "Viewm3U8Action"

Cohesion: 0.5
Nodes (1): ViewM3U8Action

### Community 267 - "Copyclipboardaction"

Cohesion: 0.5
Nodes (1): CopyClipboardAction

### Community 268 - "Abstractsubtitleentities"

Cohesion: 0.5
Nodes (3): AbstractSubtitleEntities, SubtitleEntity, SubtitleSearch

### Community 269 - "Backupworkmanager"

Cohesion: 0.5
Nodes (1): BackupWorkManager

### Community 270 - "Stringutils"

Cohesion: 0.5
Nodes (1): StringUtils

### Community 271 - "Streamplay"

Cohesion: 0.5
Nodes (2): Source, Streamplay

### Community 272 - "Internetarchive"

Cohesion: 0.5
Nodes (1): InternetArchive

### Community 273 - "Up4Stream"

Cohesion: 0.5
Nodes (2): Up4FunTop, Up4Stream

### Community 274 - "Odnoklassnikiextractor"

Cohesion: 0.5
Nodes (2): Odnoklassniki, OkRuVideo

### Community 275 - "Mediafire"

Cohesion: 0.5
Nodes (1): Mediafire

### Community 276 - "Userscloud"

Cohesion: 0.5
Nodes (1): Userscloud

### Community 277 - "Minoplres"

Cohesion: 0.5
Nodes (2): File, Minoplres

### Community 278 - "Evolaod"

Cohesion: 0.5
Nodes (2): Evoload, Evoload1

### Community 279 - "Gupload"

Cohesion: 0.5
Nodes (2): GUpload, VideoInfo

### Community 280 - "Multiquality"

Cohesion: 0.5
Nodes (1): MultiQuality

### Community 281 - "Hdmomplayerextractor"

Cohesion: 0.5
Nodes (2): HDMomPlayer, Track

### Community 282 - "Streamembed"

Cohesion: 0.5
Nodes (2): Details, StreamEmbed

### Community 283 - "Moviehab"

Cohesion: 0.5
Nodes (2): Moviehab, MoviehabNet

### Community 284 - "Trstxextractor"

Cohesion: 0.5
Nodes (2): TRsTX, TrstxVideoData

### Community 285 - "Acefile"

Cohesion: 0.5
Nodes (2): Acefile, Source

### Community 286 - "Streamhub"

Cohesion: 0.5
Nodes (1): Streamhub

### Community 287 - "Mvidoo"

Cohesion: 0.5
Nodes (1): Mvidoo

### Community 288 - "Pixeldrainextractor"

Cohesion: 0.5
Nodes (2): PixelDrain, PixelDrainDev

### Community 289 - "Streamoupload"

Cohesion: 0.5
Nodes (2): File, StreamoUpload

### Community 290 - "Yourupload"

Cohesion: 0.5
Nodes (2): ResponseSource, YourUpload

### Community 291 - "Fastream"

Cohesion: 0.5
Nodes (1): Fastream

### Community 292 - "Sobreatsesuypextractor"

Cohesion: 0.5
Nodes (2): Sobreatsesuyp, SobreatsesuypVideoData

### Community 293 - "Uservideo"

Cohesion: 0.5
Nodes (2): Sources, Uservideo

### Community 294 - "Supervideo"

Cohesion: 0.5
Nodes (2): Files, Supervideo

### Community 295 - "Blogger"

Cohesion: 0.5
Nodes (2): Blogger, ResponseSource

### Community 296 - "Hdplayersystemextractor"

Cohesion: 0.5
Nodes (2): HDPlayerSystem, SystemResponse

### Community 297 - "Playltxyz"

Cohesion: 0.5
Nodes (2): PlayLtXyz, ResponseData

### Community 298 - "Webviewresolver"

Cohesion: 0.5
Nodes (1): WebViewResolver

### Community 299 - "Watchtype"

Cohesion: 0.67
Nodes (2): SyncWatchType, WatchType

### Community 300 - "Homescrolltransformer"

Cohesion: 0.67
Nodes (1): HomeScrollTransformer

### Community 301 - "Libraryscrolltransformer"

Cohesion: 0.67
Nodes (1): LibraryScrollTransformer

### Community 302 - "Searchhelper"

Cohesion: 0.67
Nodes (1): SearchHelper

### Community 303 - "Syncsearchviewmodel"

Cohesion: 0.67
Nodes (2): SyncSearchResultSearchResponse, SyncSearchViewModel

### Community 304 - "Downloadbuttonsetup"

Cohesion: 0.67
Nodes (1): DownloadButtonSetup

### Community 305 - "Progressbaranimation"

Cohesion: 0.67
Nodes (1): ProgressBarAnimation

### Community 306 - "Accountselectlinearitemdecoration"

Cohesion: 0.67
Nodes (1): AccountSelectLinearItemDecoration

### Community 307 - "Outlinespan"

Cohesion: 0.67
Nodes (1): OutlineSpan

### Community 308 - "Extractorlinkgenerator"

Cohesion: 0.67
Nodes (1): ExtractorLinkGenerator

### Community 309 - "Roundedbackgroundcolorspan"

Cohesion: 0.67
Nodes (1): RoundedBackgroundColorSpan

### Community 310 - "Fixednextrenderersfactory"

Cohesion: 0.67
Nodes (1): FixedNextRenderersFactory

### Community 311 - "Downloadfilegenerator"

Cohesion: 0.67
Nodes (1): DownloadFileGenerator

### Community 312 - "Sourceprioritydialog"

Cohesion: 0.67
Nodes (1): SourcePriorityDialog

### Community 313 - "Linearrecycleviewlayoutmanager"

Cohesion: 0.67
Nodes (1): LinearRecycleViewLayoutManager

### Community 314 - "Ophimplugin"

Cohesion: 0.67
Nodes (1): OPhimPlugin

### Community 315 - "Locallist"

Cohesion: 0.67
Nodes (1): LocalList

### Community 316 - "Intenthelpers"

Cohesion: 0.67
Nodes (0):

### Community 317 - "Snackbarhelper"

Cohesion: 0.67
Nodes (1): SnackbarHelper

### Community 318 - "Videodownloadrestartreceiver"

Cohesion: 0.67
Nodes (1): VideoDownloadRestartReceiver

### Community 319 - "Lifecycle"

Cohesion: 0.67
Nodes (0):

### Community 320 - "Contexthelper"

Cohesion: 0.67
Nodes (0):

### Community 321 - "Webviewresolver"

Cohesion: 0.67
Nodes (1): WebViewResolver

### Community 322 - "Syncredirector"

Cohesion: 0.67
Nodes (1): SyncRedirector

### Community 323 - "Vidmoxyextractor"

Cohesion: 0.67
Nodes (1): VidMoxy

### Community 324 - "Maxstream"

Cohesion: 0.67
Nodes (1): Maxstream

### Community 325 - "Sibnetextractor"

Cohesion: 0.67
Nodes (1): SibNet

### Community 326 - "Vido"

Cohesion: 0.67
Nodes (1): Vido

### Community 327 - "Playervoxzer"

Cohesion: 0.67
Nodes (1): PlayerVoxzer

### Community 328 - "Goodstreamextractor"

Cohesion: 0.67
Nodes (1): GoodstreamExtractor

### Community 329 - "Streamsilk"

Cohesion: 0.67
Nodes (1): StreamSilk

### Community 330 - "Wibufile"

Cohesion: 0.67
Nodes (1): Wibufile

### Community 331 - "Vidnest"

Cohesion: 0.67
Nodes (1): VidNest

### Community 332 - "Vkextractor"

Cohesion: 0.67
Nodes (1): VkExtractor

### Community 333 - "Gamovideo"

Cohesion: 0.67
Nodes (1): GamoVideo

### Community 334 - "Mp4Upload"

Cohesion: 0.67
Nodes (1): Mp4Upload

### Community 335 - "Upstreamextractor"

Cohesion: 0.67
Nodes (1): UpstreamExtractor

### Community 336 - "Genericm3U8"

Cohesion: 0.67
Nodes (1): GenericM3U8

### Community 337 - "Embedgram"

Cohesion: 0.67
Nodes (1): Embedgram

### Community 338 - "M3U8Manifest"

Cohesion: 0.67
Nodes (1): M3u8Manifest

### Community 339 - "Rapidvidextractor"

Cohesion: 0.67
Nodes (1): RapidVid

### Community 340 - "Cloudmailruextractor"

Cohesion: 0.67
Nodes (1): CloudMailRu

### Community 341 - "Krakenfiles"

Cohesion: 0.67
Nodes (1): Krakenfiles

### Community 342 - "Sbplay"

Cohesion: 0.67
Nodes (1): SBPlay

### Community 343 - "Vtbe"

Cohesion: 0.67
Nodes (1): Vtbe

### Community 344 - "Emturbovidextractor"

Cohesion: 0.67
Nodes (1): EmturbovidExtractor

### Community 345 - "Sendvid"

Cohesion: 0.67
Nodes (1): Sendvid

### Community 346 - "Contentxextractor"

Cohesion: 0.67
Nodes (1): ContentX

### Community 347 - "Watchsb"

Cohesion: 0.67
Nodes (1): WatchSB

### Community 348 - "Vstreamhubhelper"

Cohesion: 0.67
Nodes (1): VstreamhubHelper

### Community 349 - "Asianembedhelper"

Cohesion: 0.67
Nodes (1): AsianEmbedHelper

### Community 350 - "Contexthelper"

Cohesion: 0.67
Nodes (0):

### Community 351 - "Contexthelper"

Cohesion: 0.67
Nodes (0):

### Community 352 - "Providertests"

Cohesion: 1.0
Nodes (1): ProviderTests

### Community 353 - "Directorypicker"

Cohesion: 1.0
Nodes (0):

### Community 354 - "Vidhideextractor"

Cohesion: 1.0
Nodes (1): VidhideExtractor

### Community 355 - "Hdstreamableextractor"

Cohesion: 1.0
Nodes (1): HDStreamAble

### Community 356 - "Coroutines"

Cohesion: 1.0
Nodes (0):

### Community 357 - "Coroutines"

Cohesion: 1.0
Nodes (0):

### Community 358 - "Videodownloadhelper"

Cohesion: 1.0
Nodes (0):

### Community 359 - "Downloadfileworkmanager"

Cohesion: 1.0
Nodes (0):

### Community 360 - "Cloudstreamplugin"

Cohesion: 1.0
Nodes (0):

## Knowledge Gaps

- **741 isolated node(s):** `ProviderTests`, `FocusTarget`, `AcraApplication`, `FocusDirection`,
  `SavedLoadResponse` (+736 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Providertests`** (2 nodes): `ProviderTests.kt`, `ProviderTests`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Directorypicker`** (2 nodes): `DirectoryPicker.kt`, `getChooseFolderLauncher()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Vidhideextractor`** (2 nodes): `VidhideExtractor.kt`, `VidhideExtractor`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Hdstreamableextractor`** (2 nodes): `HDStreamAbleExtractor.kt`, `HDStreamAble`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Coroutines`** (2 nodes): `Coroutines.jvm.kt`, `runOnMainThreadNative()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Coroutines`** (2 nodes): `Coroutines.android.kt`, `runOnMainThreadNative()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Videodownloadhelper`** (1 nodes): `VideoDownloadHelper.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Downloadfileworkmanager`** (1 nodes): `DownloadFileWorkManager.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Cloudstreamplugin`** (1 nodes): `CloudstreamPlugin.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions

_Questions this graph is uniquely positioned to answer:_

- **What connects `ProviderTests`, `FocusTarget`, `AcraApplication` to the rest of the system?**
  _741 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `AniList Sync API` be split into smaller, more focused modules?**
  _Cohesion score 0.02 - nodes in this community are weakly interconnected._
- **Should `Simkl Tracking API` be split into smaller, more focused modules?**
  _Cohesion score 0.02 - nodes in this community are weakly interconnected._
- **Should `Result ViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.03 - nodes in this community are weakly interconnected._
- **Should `MainAPI Provider System` be split into smaller, more focused modules?**
  _Cohesion score 0.03 - nodes in this community are weakly interconnected._
- **Should `Fullscreen Player UI` be split into smaller, more focused modules?**
  _Cohesion score 0.03 - nodes in this community are weakly interconnected._
- **Should `ExoPlayer Integration` be split into smaller, more focused modules?**
  _Cohesion score 0.03 - nodes in this community are weakly interconnected._