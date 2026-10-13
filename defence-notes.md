# Kaitsmise märkmed — Tic-Tac-Toe (Android 02, Jetpack Compose)

Lihtsas keeles: mida see projekt teeb, miks ta nii on ehitatud ja mida õppejõud tõenäoliselt küsib. Põhineb XML-versiooni märkmetel — mängureeglid on **sama fail**, erinev on ainult kasutajaliidese pool. Kood ja failinimed inglise keeles, nagu projektis.

---

## 1. Mis projekt see on

- Kodutöö **Android 02**: sama Tic-Tac-Toe, aga kasutajaliides **Jetpack Compose'is** (mitte XML).
- Pakett `ee.taltech.tictactoecompose`, minSdk 26, targetSdk 37, JVM 17 (Compose nõuab).
- Kaks haru:
  - `assignment-02-no-bonuses` — põhinõuded.
  - `assignment-02-with-bonuses` — + minimax, suvaline laua suurus, undo, laua pööramine.
- Teegid (kõik versioonikataloogis `gradle/libs.versions.toml`): Compose BOM + Material 3, `lifecycle-viewmodel-compose`, `lifecycle-runtime-compose`, **Navigation 3**, **DataStore Preferences**, `kotlinx.serialization` (navigatsiooni võtmete jaoks), testideks JUnit, `coroutines-test`, Compose UI test.

## 2. Failide kaart

| Fail | Mida teeb |
|---|---|
| `TicTacToeGame.kt` | **Mängureeglid.** Täpselt sama fail mis XML-äpis (muudetud ainult `package` rida). Puhas Kotlin, mitte ühtegi Androidi importi. |
| `MinimaxAi.kt` (boonus) | Minimax + alpha-beta, samuti XML-äpist kopeeritud. |
| `Statistics.kt` | Statistika numbrid `data class`-is + keskmine. Kopeeritud. |
| `data/GameStatsRepository.kt` | **Liides** (interface): mida statistika hoidla oskab — `statistics: Flow<Statistics>`, `recordGame`, `reset`. |
| `data/DataStoreGameStatsRepository.kt` | Liidese teostus **DataStore Preferences** peal (SharedPreferences asendaja Compose-ajastul). |
| `ui/game/GameUiState.kt` | Üks `data class`, mis kirjeldab kogu mänguekraani seisu (laud, kelle käik, staatus, võidulahtrid, režiim, taimer, kas dialoog on lahti…). |
| `ui/game/GameViewModel.kt` | **Mängu aju.** Hoiab `TicTacToeGame` objekti, reageerib sündmustele (`onCellClick`, `onNewGameClick`…) ja avaldab uue `GameUiState`-i `StateFlow` kaudu. Elab pööramise üle. |
| `ui/game/GameScreen.kt` | Mänguekraani **välimus**: `GameRoute` (ühendab ViewModeli) + `GameScreen` (puhas joonistamine seisust) + `InfoPanel`, `Board`, `BoardCell`, `GameTimer`, dialoogid, previewd. |
| `ui/stats/StatsViewModel.kt`, `StatsScreen.kt` | Statistika ekraan samal mustril: Route + Screen. |
| `ui/navigation/Keys.kt`, `TicTacToeNavigation.kt` | Kaks ekraani (`GameKey`, `StatsKey`) ja Navigation 3 `NavDisplay`, mis otsustab, kumb on ekraanil. |
| `ui/theme/Color.kt`, `Theme.kt` | Material 3 teema + **`GameColors`** (laua jooned, lahtri taust, võidulahter, sümboli värv) heledas ja pimedas variandis. |
| `MainActivity.kt` | Ainus Activity: `enableEdgeToEdge()`, loob hoidla, `setContent { Theme { Navigation } }`. |
| `res/values/strings.xml` | Kõik tekstid. Koodis ei ole kõvasid tekste (`stringResource`). |
| `src/test/...` | JUnit: reeglid, minimax, statistika, **ViewModel** (fake hoidlaga, ilma Androidita). |
| `src/androidTest/...` | Compose UI testid emulaatoris (`createAndroidComposeRule`). |

## 3. Kuidas üks käik läbi süsteemi liigub

1. Kasutaja puudutab lahtrit. `BoardCell` on `Box`, millel on `Modifier.clickable { onCellClick(row, column) }`.
2. `GameScreen` ei tee ise midagi — ta annab sündmuse edasi lambda kaudu üles `GameRoute`-i, kus see on seotud `viewModel::onCellClick`.
3. `GameViewModel.onCellClick` ütleb mängule `game.makeMove(row, column)`. Mäng otsustab, kas käik on lubatud ja kas keegi võitis.
4. ViewModel teeb mängust uue `GameUiState` koopia (`publishGame()`) ja paneb selle `_uiState.update { … }`-ga välja.
5. `GameRoute` kuulab `uiState.collectAsStateWithLifecycle()`. Kui seis muutub, joonistab Compose ekraani uuesti (**recomposition**). Keegi ei kutsu `setText`-i ega `setBackgroundColor`-it — ekraan on alati seisu funktsioon.
6. Mängu lõpus salvestab ViewModel tulemuse hoidlasse; AI-režiimis planeeritakse AI käik 400 ms hiljem.

Põhimõte: **seis voolab alla (state down), sündmused voolavad üles (events up).** Composable'id ei hoia mängu seisu, nad ainult näitavad seda.

---

## 4. Tõenäolised küsimused ja vastused

### Compose üldiselt

**Mis vahe on XML-i ja Compose'i vahel?**
XML-is kirjeldad vaated failis, leiad need `findViewById`-ga üles ja muudad neid käsitsi (`text = …`). Compose'is kirjutad funktsiooni, mis **kirjeldab**, kuidas ekraan mingi seisu juures välja näeb; kui seis muutub, kutsub Compose funktsiooni ise uuesti. Pole id-sid, pole `findViewById`-d, pole "uuenda nupp 5 tekst".

**Mis on `@Composable`?**
Märge, et funktsioon kirjeldab UI-d ja teda võib kutsuda ainult teiste composable'ite seest. Nimed on PascalCase (`GameScreen`), tagastavad `Unit`, esimene vabatahtlik parameeter on `modifier: Modifier = Modifier`.

**Mis on `Modifier` ja miks järjekord loeb?**
Ahel seadeid elemendi kohta (suurus, taust, padding, klikk). Iga modifier "pakib" järgmise: `.background(...).clickable(...)` värvib ja teeb klikitavaks; `.padding(16.dp).background()` jätab paddingu värvimata, `.background().padding()` värvib ka paddingu. Laual: `.weight(1f).fillMaxHeight().background(color).clickable { }`.

**Mis on recomposition?**
Compose kutsub composable'i uuesti, kui mõni tema loetud seis (`State`) muutus. Muudab ainult need osad, mille sisendid muutusid. Sellepärast peavad composable'id olema kiired ja ilma kõrvalmõjudeta.

**Mis on `remember` ja `rememberSaveable`?**
`remember` hoiab väärtust recomposition'ite vahel (muidu läheks muutuja iga joonistusega nulli). `rememberSaveable` hoiab seda ka pööramise üle (Bundle'is). Kasutan `rememberSaveable`-t laua seadete dialoogi mustandväärtuste jaoks.

**Mis on `LaunchedEffect`?**
Korutiin, mis käivitub, kui composable ekraanile tuleb, ja katkestatakse, kui ta ära läheb või võti muutub. Taimer: `LaunchedEffect(gameStartTimeMs, isInProgress) { while (…) { nowMs = …; delay(1000) } }` — uuendab ainult **näitu** kord sekundis; aeg ise arvutatakse ajatemplitest.

### Paigutus

**Kuidas portree ja landscape töötavad ilma kahe XML-failita?**
`BoxWithConstraints` annab saadaoleva laiuse ja kõrguse. `if (maxWidth > maxHeight) Row { InfoPanel; Board } else Column { InfoPanel; Board }`. Üks `if`, mitte kaks faili. Mõlemad pooled saavad `Modifier.weight(1f)` → 50/50 nagu XML-is Guideline.

**Kuidas laud ruudukujuline on?**
`Column(Modifier.aspectRatio(1f))` — Compose arvutab suurima ruudu, mis mahub. Read on `Row(Modifier.weight(1f))`, lahtrid `Box(Modifier.weight(1f).fillMaxHeight())` → kõik võrdsed.

**Miks laua ümber raami ei ole?**
Jooned on `Column`-i taust (`gameColors.boardLines`), mis paistab `Arrangement.spacedBy(3.dp)` vahedest läbi. Äärmised lahtrid lähevad Column'i servani, servas tausta ei paista. Sama idee kui XML-is, aga 10 rida koodi vähem.

**Kuidas "state hoisting" siin välja näeb?**
`GameScreen(state, onCellClick, onNewGameClick, …)` — kogu seis tuleb parameetrina sisse, kõik, mis juhtub, läheb lambdana välja. Nii on `GameScreen` ilma ViewModelita previewtav (`@PreviewLightDark`, landscape preview) ja testitav.

### ViewModel ja seis

**Miks ViewModel ja mitte seis Activity's / composable'is?**
Kursuse reegel: "mängureeglid ei kuulu composable'isse". ViewModel: 1) elab pööramise üle, seega ei ole vaja `onSaveInstanceState`-i; 2) on testitav JVM-is ilma Compose'ita (16 testi); 3) ekraan jääb rumalaks ja lihtsaks.

**Mis on `StateFlow` ja miks `MutableStateFlow` on `private`?**
Voog, millel on alati "praegune väärtus" ja mis teatab muutustest. ViewModelis `private val _uiState = MutableStateFlow(GameUiState())` (ainult ViewModel tohib muuta) ja `val uiState: StateFlow = _uiState.asStateFlow()` (teised ainult loevad). Muutmine `_uiState.update { state -> state.copy(...) }` — aatomiline, alati uus muutumatu objekt.

**Miks `GameUiState` on `data class` ja `copy`?**
Muutumatu (immutable) objekt: vana seisu ei muudeta, tehakse uus koopia muudetud väljadega. Compose võrdleb vana ja uut ning joonistab muutunud osa. Lihtne mõelda: "seis on hetkepilt".

**Miks ViewModelis on `TicTacToeGame` objekt JA `GameUiState`?**
`TicTacToeGame` on reeglid (sama kood XML-äpiga, muudetav objekt). `GameUiState` on ekraani jaoks tehtud hetkepilt sellest. `publishGame()` kopeerib mängu olulised väljad seisu. Nii saan reegleid taaskasutada ilma neid Compose'i jaoks ümber kirjutamata.

**`collectAsStateWithLifecycle()` vs `collectAsState()`?**
Esimene lõpetab kuulamise, kui äpp on taustal; teine kuulaks edasi. Kursuse soovitus on esimene.

**Kuidas sündmusi nimetatakse?**
Selle järgi, **mis juhtus**, mitte mida teha: `onCellClick`, `onNewGameConfirmed`, `onVsAiSelectedChange`. ViewModel otsustab, mida see tähendab.

**Režiimi lüliti — miks kaks välja (`isVsAiSelected` ja `isVsAi`)?**
`isVsAiSelected` on lüliti asend (kasutaja valik), `isVsAi` on käimasoleva mängu režiim. Lüliti keeramine keset mängu ei muuda käimasolevat mängu; uus mäng võtab valiku kasutusele. XML-is tegi seda `Switch` ise + `startNewGame` luges väärtuse.

### AI viivitus

**Kuidas 400 ms viivitus Compose-versioonis töötab?**
`viewModelScope.launch { delay(400); makeAiMove(); onMoveMade() }` — korutiin ViewModeli skoobis. `delay` ei blokeeri UI lõime (erinevalt `Thread.sleep`). `aiMoveJob?.cancel()` uue mängu / undo korral tühistab ootel käigu. ViewModel elab pööramise üle, seega pööramise ajal planeeritud käik jääb lihtsalt alles — XML-i "planeeri uuesti" trikki pole vaja.

**Miks `viewModelScope`?**
Skoop, mis tühistatakse automaatselt, kui ViewModel hävib. Mitte `GlobalScope` (lekib), mitte `lifecycleScope` (sureks pööramisel).

### Taimer

**Kuidas aeg töötab?**
Seisus on `gameStartTimeMs` (esimese käigu ajatempel, `SystemClock.elapsedRealtime()`) ja `finishedGameDurationMs`. Kestus = vahe. Kuvamine: `GameTimer` composable, milles `LaunchedEffect` uuendab `nowMs`-i kord sekundis, kuni mäng käib. "Salvesta ajatemplid, mitte tiksuja" on kursuse reegel. Pööramine ei sega, sest ajatempel on ViewModelis.

### Statistika ja DataStore

**Miks DataStore, mitte SharedPreferences?**
Kursus ütleb, et Compose-ajastul asendab DataStore SharedPreferences'i: asünkroonne (ei blokeeri UI lõime), annab `Flow`-i (ekraan uueneb ise, kui andmed muutuvad), vigade käsitlus `catch`-iga.

**Kuidas see koodis on?**
`val Context.gameStatsDataStore by preferencesDataStore(name = "game_stats")` — üks eksemplar faili kohta (top-level). Võtmed `intPreferencesKey("wins_x")` jne `private object Keys` sees. Lugemine: `dataStore.data.catch { … }.map { preferences -> Statistics(...) }` → `Flow<Statistics>`. Kirjutamine: `dataStore.edit { preferences -> preferences[Keys.WINS_X] = … }` — `suspend`, aatomiline.

**Miks liides `GameStatsRepository`?**
ViewModel sõltub liidesest, mitte DataStore'ist. Testis annan `FakeGameStatsRepository` (mälus list) — ViewModeli testid jooksevad JVM-is ilma Androidita. Nimetamine kursuse järgi: `interface XxxRepository` + `DataStoreXxxRepository`.

**Kuidas statistika ekraan andmed saab?**
`StatsViewModel`: `statsRepository.statistics.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Statistics())`. Kuulamine algab, kui ekraan vaatab; 5 s pärast taustale minekut lõpetab. Reset = `viewModelScope.launch { repository.reset() }`, ekraan uueneb Flow kaudu ise.

**Miks Hilti ei ole?**
Kursuse järgi on Hilt HW2-s vabatahtlik. ViewModelid luuakse `viewModel(factory = viewModelFactory { initializer { GameViewModel(statsRepository) } })` — tehas, mis teab, kuidas konstruktorile hoidla anda. Hoidla tehakse ühe korra `MainActivity`-s.

### Navigatsioon

**Mis on Navigation 3?**
Kursuse nõutud navigatsioon. Back stack on **tavaline list** võtmetest: `rememberNavBackStack(GameKey)`. Edasi: `backStack.add(StatsKey)`. Tagasi: `backStack.removeLastOrNull()`. `NavDisplay` joonistab listi viimase võtme ekraani.

**Mis on `GameKey` / `StatsKey`?**
`@Serializable data object GameKey : NavKey` — võti, mis ütleb "mänguekraan". Serialiseeritav, et back stack pööramise üle elaks (sellepärast `kotlinx.serialization` plugin).

**Miks ekraanid back stacki ei näe?**
`GameRoute` saab `onOpenStats: () -> Unit` lambda; mida see teeb, otsustab navigatsioon. Ekraan ei tea, et back stack olemas on. "Back to game" = `backStack.removeLastOrNull()`, sama mis süsteemi tagasi-nupp (`onBack`).

**Mis need `entryDecorators` on?**
`rememberSaveableStateHolderNavEntryDecorator()` hoiab ekraani `rememberSaveable` väärtused alles; `rememberViewModelStoreNavEntryDecorator()` annab igale ekraanile oma ViewModeli, mis elab nii kaua kui ekraan back stackis on. Järjekord on kursuse järgi just selline.

### Teema

**Kuidas dark mode töötab?**
`isSystemInDarkTheme()` valib `darkColorScheme` / `lightColorScheme` ja `DarkGameColors` / `LightGameColors`. Laua värvid on `@Immutable data class GameColors`, antud edasi `staticCompositionLocalOf`-iga (`LocalGameColors.current`). Mitte `CompositionLocal` andmete jaoks, ainult teema jaoks — kursuse reegel.

**Miks `dynamicColor` on välja lülitatud?**
Androidi dünaamiline värv võtaks taustapildi toonid; laua värvid kannavad tähendust (võidulahter roheline), neid ei tohi süsteem ümber värvida.

**Kus on `values-night`?**
Pole vaja. Compose'is on värvid Kotlini koodis (`Color.kt`), tume variant on sama failis teise `GameColors` objektina.

### Testid

- JVM: `TicTacToeGameTest`, `MinimaxAiTest`, `StatisticsTest` (kopeeritud) + `GameViewModelTest` (16 testi: käik, võit + salvestus, AI viivitus `advanceTimeBy`-ga, dialoog, undo, seaded).
- `Dispatchers.setMain(StandardTestDispatcher())` — `viewModelScope` vajab testis main-dispatcherit; `runTest` + `advanceTimeBy(500)` kerib virtuaalset aega, päris 400 ms ootamist ei ole.
- Compose UI testid: `createAndroidComposeRule<MainActivity>()`, elemendid leitakse `testTag`-iga (`cell_0_0`, `buttonNewGame`), `performClick()`, `assertTextEquals`. `scenario.recreate()` simuleerib pööramist.
- Käsud: `./gradlew test`, `./gradlew connectedAndroidTest`, `./gradlew lint`.

---

## 5. Boonused (ainult `assignment-02-with-bonuses`)

### Suvaline laua suurus
- Reeglid XML-ist kopeeritud. `GameUiState.boardSettings` = `BoardSettings(rowCount, columnCount, winLength)`.
- `Board` composable käib `state.board` ridu ja veerge tsükliga läbi — mitte midagi ei ole "9 lahtrit" kõvasti kirjas, seega suurem laud tuli peaaegu tasuta. Mitteruudukujuline: `Column(Modifier.aspectRatio(columns / rows))` ruudu sees. Tekst skaleerub `40.sp × 3 / suuremKülg`.
- Seaded: `AlertDialog` oma sisuga: kolm `NumberStepper`-it (− väärtus +; Compose'is NumberPickerit ei ole). Mustand `rememberSaveable`-s, `Apply` → `onBoardSettingsApplied(BoardSettings(...))`. Võidurida piiratakse suurema küljega. Tühjal laual rakendub kohe, muidu järgmisest mängust.

### Undo
- `onUndoClick`: `game.undoMove()`; AI-režiimis kaks korda (AI käik + enda käik); moveCount 0 → taimer nulli; ootel AI käik tühistatakse. Nupp `enabled = state.canUndo` (mäng käib ja käike > 0). Lõppenud mängus undo ei tööta, sest tulemus on juba statistikas.

### Laua pööramine
- Seisus `boardRotationDegrees` (Float, +90 iga vajutusega). Ekraanil `animateFloatAsState(targetValue = state.boardRotationDegrees)` → `Modifier.graphicsLayer { rotationZ = … }` laua ümbrisel. Animeeritakse **sihtnurgani**, mitte "+90 praegusest", seega kiire topeltvajutus lõpeb 180° peal.
- `graphicsLayer` pöörab ka puudutuste koordinaate, lahtrid jäävad klikitavaks (testis kontrollitud).

### Minimax
- Sama `MinimaxAi` mis XML-is: `makeMove → rekursioon → undoMove`, alpha-beta, 3×3 lõpuni, suurel laual piiratud sügavus + hindamisfunktsioon (avatud võiduaknad) + ainult nuppude kõrval olevad lahtrid. ViewModelis: `MinimaxAi.forBoard(game).chooseMove(game)` — AI mängib **alati** minimaxiga, valikut ei ole. `makeRandomMove()` jäi mootorisse ainult testide jaoks.
- Jookseb `viewModelScope` korutiinis main-lõimel pärast `delay`-d; mõõdetult < 100 ms, seega taustalõime ei teinud.

---

## 6. Teadaolevad piirangud

- Laua seaded, režiim ja alustaja ei säili äpi sulgemisel (ViewModel elab pööramise, mitte protsessi surma üle). Nõutud oli ainult statistika püsivus — see on DataStore'is.
- Taimer näitab `0:04`, XML-is näitas Chronometer `00:04`. Vormingut saab muuta `duration_format` stringis.
- Compose UI testides on kaks ootamist (`waitUntil` AI käigule, `mainClock.advanceTimeBy` pöördele).

## 7. Käsud

```bash
./gradlew assembleDebug          # build
./gradlew test                   # JUnit testid (arvutis)
./gradlew connectedAndroidTest   # Compose UI testid (emulaator peab käima)
./gradlew lint                   # Android Lint

# pime teema emulaatoris (Settings → Display → Advanced → Dark theme on peidus)
~/Library/Android/sdk/platform-tools/adb shell cmd uimode night yes
~/Library/Android/sdk/platform-tools/adb shell cmd uimode night no

# kui pööramine "ei tööta": kontrolli, et emulaatori auto-rotate on sees (1)
~/Library/Android/sdk/platform-tools/adb shell settings get system accelerometer_rotation
~/Library/Android/sdk/platform-tools/adb shell settings put system accelerometer_rotation 1
```
