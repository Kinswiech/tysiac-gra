import { useState, useEffect } from 'react'
import './App.css'

const SUITS = { 'HEARTS': '♥', 'DIAMONDS': '♦', 'CLUBS': '♣', 'SPADES': '♠' };
const RANKS = { 'NINE': '9', 'TEN': '10', 'JACK': 'J', 'QUEEN': 'Q', 'KING': 'K', 'ACE': 'A' };
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

function App() {
    const [game, setGame] = useState(null);
    const [playerName, setPlayerName] = useState("");
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [errorMsg, setErrorMsg] = useState("");

    useEffect(() => {
        if (!isLoggedIn) return;
        const interval = setInterval(() => fetchGame(), 1000);
        return () => clearInterval(interval);
    }, [isLoggedIn]);

    const fetchGame = async () => {
        try {
            const response = await fetch(`${API_URL}/api/game/state`);
            if (response.ok) {
                const data = await response.json();
                if (data.players && data.players.length > 0) {
                    setGame(data);
                }
            }
        } catch (e) { console.error(e); }
    };

    const joinGame = async () => {
        if (!playerName) return;
        try {
            const response = await fetch(`${API_URL}/api/game/join?playerName=${playerName}`, { method: 'POST' });
            if (response.ok) {
                setIsLoggedIn(true);
                fetchGame();
                setErrorMsg("");
            } else {
                const err = await response.json();
                setErrorMsg(err.message || "Nie można dołączyć (Stół pełny?)");
            }
        } catch (e) {
            setErrorMsg("Błąd połączenia z serwerem");
        }
    }

    const submitBid = async (amount) => {
        if (game.players[game.currentPlayerIndex].name !== playerName) {
            alert("To nie Twoja kolej!"); return;
        }

        // ZMIANA: Obsługa błędów (np. "Za wysoka licytacja")
        const response = await fetch(`${API_URL}/api/game/bid?amount=${amount}`, { method: 'POST' });

        if (!response.ok) {
            const err = await response.json();
            alert("BŁĄD: " + (err.message || "Nieprawidłowa stawka!"));
        } else {
            fetchGame();
        }
    }

    const playCard = async (rank, suit) => {
        // 1. ROZDAWANIE (Z wyborem gracza)
        if (game.phase === 'SHARING') {
            if (game.players[game.currentPlayerIndex].name !== playerName) {
                alert("To nie Ty wygrałeś licytację!"); return;
            }

            // Znajdź innych graczy
            const opponents = game.players.filter(p => p.name !== playerName);

            // Prosty prompt do wyboru (W wersji PRO byłby ładny modal)
            const targetName = prompt(
                `Komu oddać ${RANKS[rank]}${SUITS[suit]}?\nWpisz dokładnie nick:\n1. ${opponents[0].name}\n2. ${opponents[1].name}`
            );

            if (targetName && opponents.some(p => p.name === targetName)) {
                await fetch(`${API_URL}/api/game/share?rank=${rank}&suit=${suit}&targetPlayer=${targetName}`, { method: 'POST' });
                fetchGame();
            } else {
                alert("Niepoprawny nick gracza!");
            }
            return;
        }

        // 2. Licytacja
        if (game.phase === 'BIDDING') { alert("Trwa licytacja!"); return; }

        // 3. Gra
        if (game.players[game.currentPlayerIndex].name !== playerName) {
            alert("To nie Twoja tura!"); return;
        }

        const response = await fetch(`${API_URL}/api/game/play?rank=${rank}&suit=${suit}`, { method: 'POST' });
        if (!response.ok) {
            const errorData = await response.json();
            alert("BŁĄD: " + (errorData.message || "Błąd ruchu"));
        } else {
            // Po udanym ruchu od razu pobierz stan, żeby uniknąć laga
            fetchGame();
        }
    };

    const isRedSuit = (suit) => (suit === 'HEARTS' || suit === 'DIAMONDS');

    // --- EKRAN LOGOWANIA (LOBBY) ---
    if (!isLoggedIn) {
        return (
            <div className="login-screen" style={{textAlign: 'center', marginTop: '50px', color: 'black'}}>
                <h1 style={{color: 'white'}}>Witaj w Tysiącu!</h1>
                <div style={{background: 'white', padding: 20, borderRadius: 10, display: 'inline-block'}}>
                    <input
                        type="text"
                        placeholder="Wpisz swój Nick"
                        value={playerName}
                        onChange={(e) => setPlayerName(e.target.value)}
                        style={{padding: '10px', fontSize: '16px'}}
                    />
                    <button
                        onClick={joinGame}
                        style={{padding: '10px 20px', marginLeft: '10px', background: 'green', color: 'white', border:'none', cursor:'pointer'}}>
                        DOŁĄCZ DO STOŁU
                    </button>
                    {errorMsg && <p style={{color: 'red', marginTop: 10}}>{errorMsg}</p>}
                </div>
            </div>
        )
    }

    if (!game || game.players.length < 3) return (
        <div style={{color:'white', textAlign:'center', marginTop: 50}}>
            <h2>Czekamy na graczy... ({game ? game.players.length : 0}/3)</h2>
            <p>Jesteś zalogowany jako: <strong>{playerName}</strong></p>
        </div>
    );

    const isMyTurn = game.players[game.currentPlayerIndex].name === playerName;
    const currentBid = game.currentBid || 100;

    return (
        <div className="game-table">
            <div style={{position: 'absolute', top: 10, left: 10, color: 'white'}}>
                Grasz jako: <strong>{playerName}</strong>
            </div>

            {game.phase === 'PLAYING' && (
                <div style={{position: 'absolute', top: 20, right: 20, background: 'rgba(0,0,0,0.5)', padding: '10px', borderRadius: '10px'}}>
                    <small style={{color: 'white'}}>ATUT:</small>
                    {game.trumpSuit ? (
                        <div style={{color: isRedSuit(game.trumpSuit) ? '#d32f2f' : 'white', fontSize: '30px'}}>{SUITS[game.trumpSuit]}</div>
                    ) : (<div style={{color: '#ccc'}}>BRAK</div>)}
                </div>
            )}

            {/* MUSIK */}
            <div className="section">
                <h3 style={{color: 'white'}}>Musik</h3>
                <div className="hand">
                    {game.musik.map((card, i) => ( <div key={i} className="card back"></div> ))}
                </div>
            </div>

            {/* STÓŁ */}
            <div className="table-center" style={{
                minHeight: '160px', margin: '20px auto', border: '2px solid gold', borderRadius: '50%', padding: '20px', width: '350px',
                display:'flex', justifyContent:'center', alignItems: 'center', gap:'10px', background: 'rgba(0,50,0,0.3)',
                flexDirection: game.phase === 'PLAYING' ? 'row' : 'column'
            }}>

                {game.phase === 'BIDDING' && (
                    <div style={{textAlign: 'center', color: 'white'}}>
                        <h3>💰 LICYTACJA 💰</h3>
                        <p>Stawka: <strong style={{color: 'gold', fontSize: '24px'}}>{currentBid}</strong></p>
                        <p>Decyduje: <strong>{game.players[game.currentPlayerIndex].name}</strong></p>

                        {isMyTurn && (
                            <div style={{display: 'flex', gap: '10px', marginTop: '10px', justifyContent: 'center'}}>
                                <button onClick={() => submitBid(0)} style={{padding: '10px', background: '#d32f2f', color:'white', border:'none', cursor:'pointer'}}>PAS</button>
                                <button onClick={() => submitBid(currentBid + 10)} style={{padding: '10px', background: '#388e3c', color:'white', border:'none', cursor:'pointer'}}>{currentBid + 10}</button>
                            </div>
                        )}
                    </div>
                )}

                {game.phase === 'SHARING' && (
                    <div style={{textAlign: 'center', color: 'white'}}>
                        <h3>🤝 ROZDAWANIE 🤝</h3>
                        <p>Wygrał: <strong>{game.players[game.currentPlayerIndex].name}</strong></p>
                        {isMyTurn ? (
                            <div style={{color: 'gold', border: '1px dashed gold', padding: 10}}>
                                Kliknij swoją kartę, a potem wpisz imię gracza, któremu chcesz ją oddać.<br/>
                                Oddano: {game.cardsGivenCount}/2
                            </div>
                        ) : (<p>Czekaj na rozdanie...</p>)}
                    </div>
                )}

                {/* WARIANT: DEKLARACJA PUNKTÓW (NOWOŚĆ) */}
                {/* WARIANT: DEKLARACJA PUNKTÓW */}
                {game.phase === 'DECLARING' && (
                    <div style={{textAlign: 'center', color: 'white'}}>
                        <h3>DEKLARACJA</h3>
                        <p>Wygrałeś licytację kwotą: <strong>{currentBid}</strong></p>
                        {isMyTurn ? (
                            <div style={{background: 'rgba(0,0,0,0.5)', padding: 10, borderRadius: 5}}>
                                <p>Ile chcesz grać?</p>
                                <button onClick={async () => {
                                    const newBid = prompt("Podaj ostateczną stawkę (min. " + currentBid + "):", currentBid);

                                    if (newBid && !isNaN(newBid)) {
                                        // Tu zmieniliśmy na async/await, żeby złapać błąd z backendu
                                        const response = await fetch(`${API_URL}/api/game/declare?points=${newBid}`, { method: 'POST' });

                                        if (!response.ok) {
                                            // Jeśli backend rzuci błędem (np. "Za słabe karty"), wyświetlamy go
                                            const err = await response.json();
                                            alert("BŁĄD: " + (err.message || "Nieprawidłowa stawka!"));
                                        } else {
                                            fetchGame();
                                        }
                                    }
                                }} style={{padding: '10px 20px', background: 'gold', color: 'black', border: 'none', fontWeight: 'bold', cursor: 'pointer'}}>
                                    USTAL STAWKĘ
                                </button>
                            </div>
                        ) : (
                            <p>Zwycięzca ustala ostateczną stawkę...</p>
                        )}
                    </div>
                )}

                {game.phase === 'PLAYING' && (
                    <>
                        {game.table.map((card, i) => (
                            <div key={i} className={`card ${isRedSuit(card.suit) ? 'red' : 'black'}`}>
                                <span className="corner">{RANKS[card.rank] || card.rank}</span>
                                <span className="center">{SUITS[card.suit] || card.suit}</span>
                            </div>
                        ))}
                    </>
                )}
            </div>

            {/* GRACZE */}
            <div className="players-container">
                {game.players.map((player, index) => {
                    const isCurrent = index === game.currentPlayerIndex;
                    const isMe = player.name === playerName;
                    return (
                        <div key={index} className="player-area" style={{border: isCurrent ? '4px solid gold' : '2px dashed rgba(255,255,255,0.3)', opacity: isCurrent ? 1 : 0.7}}>
                            <h3 style={{color: 'white'}}>{player.name} {isCurrent ? "🔔" : ""} <span style={{float: 'right', color: 'gold'}}>{player.score} pkt</span></h3>
                            <div className="hand">
                                {player.hand.map((card, cIdx) => (
                                    isMe ? (
                                        <div key={cIdx} className={`card ${isRedSuit(card.suit) ? 'red' : 'black'}`} onClick={() => playCard(card.rank, card.suit)}>
                                            <span className="corner">{RANKS[card.rank] || card.rank}</span>
                                            <span className="center">{SUITS[card.suit] || card.suit}</span>
                                        </div>
                                    ) : (<div key={cIdx} className="card back"></div>)
                                ))}
                            </div>
                        </div>
                    )
                })}
            </div>
        </div>
    )
}

export default App