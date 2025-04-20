function formatDateTime(isoString) {
    try {
        const date = new Date(isoString);

        if (isNaN(date.getTime())) {
            return isoString;
        }

        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');

        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');

        return `${year}-${month}-${day} ${hours}:${minutes}`;
    } catch (error) {
        console.error('Error formatting date:', error);
        return isoString;
    }
}

fetch('/monitor')
    .then(response => response.json())
    .then(data => {
        const tableBody = document.querySelector('#statuses tbody');
        tableBody.innerHTML = ''; // Clear loading message

        if (data.length === 0) {
            const emptyRow = document.createElement('tr');
            const emptyCell = document.createElement('td');
            emptyCell.colSpan = 2;
            emptyCell.textContent = 'No status data available';
            emptyCell.className = 'empty-message';
            emptyRow.appendChild(emptyCell);
            tableBody.appendChild(emptyRow);
            return;
        }

        data.forEach(item => {
            const tr = document.createElement('tr');

            const tdStatus = document.createElement('td');
            tdStatus.textContent = item.source;

            const tdTime = document.createElement('td');
            // Format the timestamp to YYYY-MM-DD HH:mm in local timezone
            tdTime.textContent = formatDateTime(item.timestamp);
            tdTime.className = 'timestamp';

            tr.appendChild(tdStatus);
            tr.appendChild(tdTime);
            tableBody.appendChild(tr);
        });
    })
    .catch(error => {
        console.error('Error fetching data:', error);
        const tableBody = document.querySelector('#statuses tbody');
        tableBody.innerHTML = '';
        const errorRow = document.createElement('tr');
        const errorCell = document.createElement('td');
        errorCell.colSpan = 2;
        errorCell.textContent = 'Failed to load data. Please try again later.';
        errorCell.className = 'error-message';
        errorRow.appendChild(errorCell);
        tableBody.appendChild(errorRow);
    });