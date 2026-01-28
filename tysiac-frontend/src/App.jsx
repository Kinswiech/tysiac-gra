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
                if (data.players && data.players.length > 0) setGame(data);
            }
        } catch (e) { console.error(e); }
    };

    const joinGame = async () => {
        if (!playerName) return;
        try {
            const response = await fetch(`${API_URL}/api/game/join?playerName=${playerName}`, { method: 'POST' });
            if (response.ok) { setIsLoggedIn(true); fetchGame(); setErrorMsg(""); }
            else { const err = await response.json(); setErrorMsg(err.message || "Błąd"); }
        } catch (e) { setErrorMsg("Błąd serwera"); }
    }

    const resetServerGame = async () => {
        if (!confirm("Czy na pewno chcesz zresetować grę dla WSZYSTKICH?")) return;
        try {
            await fetch(`${API_URL}/api/game/reset`, { method: 'POST' });
            setGame(null);
            setPlayerName("");
            setIsLoggedIn(false);
        } catch (e) { alert("Błąd resetowania gry"); }
    }

    const submitBid = async (amount) => {
        if (game.players[game.currentPlayerIndex].name !== playerName) return alert("Nie Twoja kolej!");
        await fetch(`${API_URL}/api/game/bid?amount=${amount}`, { method: 'POST' });
        fetchGame();
    }

    const playCard = async (rank, suit) => {
        if (game.phase === 'SHARING') {
            if (game.players[game.currentPlayerIndex].name !== playerName) return alert("Nie Ty rozdajesz!");
            const opponents = game.players.filter(p => p.name !== playerName);
            const targetName = prompt(`Komu oddać ${RANKS[rank]}${SUITS[suit]}?\n1. ${opponents[0].name}\n2. ${opponents[1].name}`);
            if (targetName) {
                await fetch(`${API_URL}/api/game/share?rank=${rank}&suit=${suit}&targetPlayer=${targetName}`, { method: 'POST' });
                fetchGame();
            }
            return;
        }
        if (game.phase === 'BIDDING') return alert("Trwa licytacja!");
        if (game.players[game.currentPlayerIndex].name !== playerName) return alert("Nie Twoja tura!");

        const response = await fetch(`${API_URL}/api/game/play?rank=${rank}&suit=${suit}`, { method: 'POST' });
        if (!response.ok) { const err = await response.json(); alert(err.message); }
        else fetchGame();
    };

    const isRedSuit = (suit) => (suit === 'HEARTS' || suit === 'DIAMONDS');

    // --- EKRAN LOGOWANIA ---
    if (!isLoggedIn) {
        return (
            <div className="login-overlay">
                <div className="login-box">
                    <h2>Witaj w Tysiącu!</h2>
                    <input type="text" placeholder="Wpisz swój Nick" value={playerName} onChange={e => setPlayerName(e.target.value)} />
                    <button className="login-btn" onClick={joinGame}>DOŁĄCZ DO STOŁU</button>
                    {errorMsg && <div style={{color:'red', fontSize:'14px'}}>{errorMsg}</div>}
                </div>
            </div>
        )
    }

    if (!game || game.players.length < 3) return <div className="login-overlay"><h2 style={{color:'white'}}>Czekanie na graczy... ({game ? game.players.length : 0}/3)</h2></div>;

    const myPlayer = game.players.find(p => p.name === playerName);
    const opponents = game.players.filter(p => p.name !== playerName);
    const isMyTurn = game.players[game.currentPlayerIndex].name === playerName;

    return (
        <div className="game-container">

            {/* 1. PRZYCISK RESETU (LEWY GÓRNY RÓG) */}
            <button
                onClick={resetServerGame}
                style={{
                    position: 'absolute', top: '15px', left: '15px', zIndex: 9999,
                    background: '#d32f2f', color: 'white', border: 'none',
                    padding: '8px 12px', borderRadius: '5px', cursor: 'pointer', fontWeight: 'bold',
                    boxShadow: '0 2px 5px rgba(0,0,0,0.5)'
                }}
            >
                🔄 RESET
            </button>

            {/* 2. WSKAŹNIK ATUTU (NOWOŚĆ - PRAWY GÓRNY RÓG) */}
            <div style={{
                position: 'absolute', top: '15px', right: '15px', zIndex: 9999,
                background: 'rgba(0,0,0,0.4)', padding: '10px 20px', borderRadius: '10px',
                textAlign: 'center', border: '1px solid rgba(255,255,255,0.2)'
            }}>
                <div style={{fontSize:'12px', color:'#ddd', marginBottom:'5px', textTransform:'uppercase'}}>Atut</div>
                {game.trumpSuit ? (
                    <div style={{
                        fontSize: '40px',
                        lineHeight: '40px',
                        textShadow: '0 2px 5px rgba(0,0,0,0.5)',
                        color: isRedSuit(game.trumpSuit) ? '#ff5252' : 'white'
                    }}>
                        {SUITS[game.trumpSuit]}
                    </div>
                ) : (
                    <div style={{fontSize:'16px', color:'#aaa', fontWeight:'bold'}}>BRAK</div>
                )}
            </div>

            {/* --- GÓRA: PRZECIWNICY --- */}
            <div className="opponents-row">
                {opponents.map((p, i) => (
                    <div key={i} className="player-zone">
                        <div className="info-text">{p.name} {game.players[game.currentPlayerIndex].name === p.name ? "🔔" : ""}</div>
                        <div style={{color:'gold', fontWeight:'bold', marginBottom:5}}>{p.score} pkt</div>
                        <div className="hand">
                            {p.hand.map((_, idx) => <div key={idx} className="card back" style={{width:50, height:80}}></div>)}
                        </div>
                    </div>
                ))}
            </div>

            {/* --- ŚRODEK: STÓŁ I MUSIK --- */}
            <div className="table-area">
                {(game.phase === 'BIDDING' || game.phase === 'SHARING') && (
                    <div className="musik-container">
                        <div className="info-text" style={{position:'absolute', top:-25, width:'100%', textAlign:'center'}}>Musik</div>
                        {game.musik.map((c, i) => <div key={i} className="card back"></div>)}
                    </div>
                )}

                <div className="center-circle">
                    {game.phase === 'BIDDING' && (
                        <>
                            <div className="info-text">💰 LICYTACJA 💰</div>
                            <div className="info-text" style={{color:'gold', fontSize:24}}>{game.currentBid}</div>
                            <div className="info-text" style={{fontSize:12}}>Decyduje: {game.players[game.currentPlayerIndex].name}</div>
                            {isMyTurn && (
                                <div className="action-buttons">
                                    <button className="btn-action btn-pass" onClick={() => submitBid(0)}>PAS</button>
                                    <button className="btn-action btn-bid" onClick={() => submitBid(game.currentBid + 10)}>{game.currentBid + 10}</button>
                                </div>
                            )}
                        </>
                    )}

                    {game.phase === 'SHARING' && (
                        <>
                            <div className="info-text">Rozdawanie...</div>
                            {isMyTurn && <div className="info-text" style={{color:'gold', fontSize:12}}>Wybierz kartę do oddania</div>}
                        </>
                    )}

                    {game.phase === 'DECLARING' && isMyTurn && (
                        <button className="btn-action btn-gold" onClick={() => {
                            const val = prompt("Ile grasz?", game.currentBid);
                            if(val) fetch(`${API_URL}/api/game/declare?points=${val}`, {method:'POST'}).then(fetchGame);
                        }}>ZADEKLARUJ</button>
                    )}

                    {game.phase === 'PLAYING' && (
                        <div className="played-cards">
                            {game.table.map((c, i) => (
                                <div key={i} className={`card ${isRedSuit(c.suit)?'red':'black'}`}>
                                    <span className="corner">{RANKS[c.rank]}</span>
                                    <span className="center">{SUITS[c.suit]}</span>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>

            {/* --- DÓŁ: TY --- */}
            <div className="my-zone">
                <div className="my-hand-wrapper" style={{borderColor: isMyTurn ? 'gold' : 'transparent'}}>
                    <div style={{width:'100%', display:'flex', justifyContent:'space-between', marginBottom:10}}>
                        <span className="info-text" style={{fontSize:18}}>{myPlayer.name} {isMyTurn ? "🔔" : ""}</span>
                        <span className="info-text" style={{color:'gold'}}>{myPlayer.score} pkt</span>
                    </div>

                    <div className="hand">
                        {myPlayer.hand.map((c, i) => (
                            <div key={i} className={`card ${isRedSuit(c.suit)?'red':'black'}`} onClick={() => playCard(c.rank, c.suit)}>
                                <span className="corner">{RANKS[c.rank]}</span>
                                <span className="center">{SUITS[c.suit]}</span>
                            </div>
                        ))}
                    </div>
                </div>
            </div>

        </div>
    )
}

export default App