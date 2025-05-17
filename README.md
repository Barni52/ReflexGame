# Android App Követelmények Dokumentáció

## Adatmodell
- **User osztály**  
  Az alkalmazás fő adatmodellje, amely a felhasználók adatait tárolja.

---

## Animációk
1. **LoginActivity – 250. sor**  
   Egy animáció, amely a bejelentkezési folyamat során fut.
2. **GameActivity – 57. sor**  
   Egy animáció, amely játék elött történik.

---

## Lifecycle Hook
- **LeaderBoardActivity – 47. sor**
---

## Erőforrások (Resources)
1. **GameActivity – 117. sor**  
   A `GameView` 56. sorában használva: a telefon rezeg, amikor a játékos elveszít egy életet.
2. **MainMenuActivity – 83. sor**  
   Egy külső weboldalt tölt be, amely útmutatót ad a játékhoz.

---

## Rendszerszolgáltatások (System Services)
1. **NotificationHelper – GameActivity 105. sor**  
   Értesítést küld, amikor a játékos új rekordot ér el.
2. **AlarmManager – MainMenuActivity 185. sor**  
   Minden nap reggel 8:00-kor üzenetet küld, hogy ösztönözze az alkalmazás használatát.

---

## CRUD Műveletek
- **Create:** `SelectUsernameActivity` – 77. sor
- **Read:** `LeaderBoardActivity` – 53. sor
- **Update:** `GameActivity` – 104. sor
- **Delete:** `ProfileActivity` – 73. sor

---

## Komplex Lekérdezések
1. **ProfileActivity – 96. sor**
2. **LeaderBoardActivity – 58. sor**
3. **SelectUsernameActivity – 59. sor**

---

---
A maradék követelményt vagy csak teszteléssel lehet ellenőrizni vagy triviálisan megtalálható
---
