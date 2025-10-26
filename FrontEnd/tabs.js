document.addEventListener('DOMContentLoaded', function() {
    // Set default tab
    showTab('react-app.html', 0);

    // Add hover effects to tabs
    const tabs = document.getElementsByClassName('tab');
    for (let i = 0; i < tabs.length; i++) {
        tabs[i].addEventListener('mouseenter', function() {
            this.classList.add('hovered');
        });
        tabs[i].addEventListener('mouseleave', function() {
            this.classList.remove('hovered');
        });
    }

    // Attach click event to tabs for accessibility (optional)
    for (let i = 0; i < tabs.length; i++) {
        tabs[i].addEventListener('click', function() {
            showTab(this.getAttribute('data-page'), i);
        });
    }
});

function showTab(page, tabIndex) {
    document.getElementById('tab-content').src = page;
    var tabs = document.getElementsByClassName('tab');
    for (var i = 0; i < tabs.length; i++) {
        tabs[i].classList.remove('active');
    }
    tabs[tabIndex].classList.add('active');
}