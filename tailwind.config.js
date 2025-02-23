/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        "./src/main/resources/templates/**/*.html",
    ],
    theme: {
        extend: {
            colors: {
                customWhite: '#DDDDDD',
                customBlack: '#2C2C2C',
                customBlue: '#4880EE',
                customSkyBlue: '#30B0C7',
                customGray: '#D9D9D9'
            },
        },
    },
    plugins: [
        require('daisyui'),
    ],
};