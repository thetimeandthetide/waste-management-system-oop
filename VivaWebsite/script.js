const links = document.querySelectorAll("nav a");
const sections = [...links].map(link => document.querySelector(link.getAttribute("href")));

function updateActiveLink() {
    let currentIndex = 0;

    sections.forEach((section, index) => {
        if (section && section.getBoundingClientRect().top < 160) {
            currentIndex = index;
        }
    });

    links.forEach(link => link.classList.remove("active"));
    links[currentIndex]?.classList.add("active");
}

document.addEventListener("scroll", updateActiveLink);
updateActiveLink();
