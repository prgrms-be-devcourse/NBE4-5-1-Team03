
document.addEventListener('DOMContentLoaded', function() {
    const backButton = document.getElementById('backButton');
    const userEmail = backButton.dataset.email;
    backButton.addEventListener('click', function() {
        const previousPage = document.referrer;

        if (previousPage.includes('/orders/order-list')) {
            history.back();
        } else {
            const encodedEmail = encodeURIComponent(userEmail);
            location.href = `/orders/order-list?email=${encodedEmail}`;
        }
    });
});