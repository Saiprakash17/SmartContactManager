console.log('Hello from script.js');

let currentTheme = getTheme();

document.addEventListener("DOMContentLoaded", () => {
    toggleTheme();
});

function toggleTheme() {
    //let theme = getTheme();
    //initialize the theme
    applyTheme();
    //set the listener to theme button
    const themeButton = document.querySelector("#themeButton");   
    themeButton.addEventListener("click", (event) => {
        console.log("Theme button clicked");
        console.log("Current theme: " + currentTheme);
        if (currentTheme === "light") {
            currentTheme = "dark";
        } else {
            currentTheme = "light";
        }
        setTheme(currentTheme);
        console.log("New theme: " + getTheme());
        applyTheme();
    });

    
}

//set theme to localstorage
function setTheme(theme) {
    localStorage.setItem("theme", theme);
}

//get theme from localstorage
function getTheme() {
    let theme = localStorage.getItem("theme");
    console.log("Theme from local storage: " + theme);
    return theme ? theme : "light";
}

//apply theme to the page
function applyTheme() {
    let theme = getTheme();
    if (theme === "light") {
        document.documentElement.classList.remove("dark");
        } else {
        document.documentElement.classList.add("dark");
    }
    document
    .querySelector("#themeButton")
    .querySelector("span").textContent = theme == "light" ? "Dark" : "Light";
    console.log("Theme applied: " + theme);
}